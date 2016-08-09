package jStrg.GWT.server.service;


import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import jStrg.GWT.client.service.Service;
import jStrg.file_system.*;
import jStrg.network_management.storage_management.CacheFileLock;
import jStrg.network_management.storage_management.core.Location;
import jStrg.network_management.storage_management.core.StorageCell;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FilenameUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by Jura on 15.05.2016.
 */
public class ServiceImpl extends RemoteServiceServlet implements Service {

    private final static Logger LOGGER = Logger.getLogger(Settings.location_logging_target);

    private static final long UID = 4L;

    /**
     * Methode to handel incoming POST with multiple Fileupload content
     *
     * @param request request date with form fields values
     * @param response  message back to the client
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //fileupload?
        boolean isMultiPart = ServletFileUpload.isMultipartContent(new ServletRequestContext(request));

        if (isMultiPart) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);

            try {
                List<FileItem> items = upload.parseRequest(request);
                int parent_id = -1; // -1 on update
                int file_id = -1; //-1 on create
                User contextUser = null;
                FileItem upload_formdata = null;

                //getting data from form
                for (FileItem item : items) {
                    if (item.isFormField()) {
//                        if (item.getFieldName().equalsIgnoreCase("user_id"))
//                            user_id = Integer.parseInt(Streams.asString(item.getInputStream()));
                        if (item.getFieldName().equalsIgnoreCase("parent_id"))
                            parent_id = Integer.parseInt(Streams.asString(item.getInputStream()));
                        if (item.getFieldName().equalsIgnoreCase("file_id"))
                            file_id = Integer.parseInt(Streams.asString(item.getInputStream()));

                    } else {
                        upload_formdata = item; // save last element
                    }
                }
                String fileName = upload_formdata.getName();
                long fileSize = upload_formdata.getSize();

                //get user from session
                HttpSession session = request.getSession();
                Object userObj = session.getAttribute("user");  // get user from session
                if (userObj != null && userObj instanceof User) {
                    contextUser = (User) userObj;
                }
                // get context

                FileFolder contextFolder = null;
                File filesystem_file = null; // File object in db
                if (parent_id == -1 && file_id != -1) {
                    filesystem_file = File.find(file_id);
                    contextFolder = filesystem_file.get_parent();
                } else {
                    contextFolder = FileFolder.find(parent_id);
                }

                if (!fileName.isEmpty()) {
                    fileName = FilenameUtils.getName(fileName);
                }

                if (fileName.equals("")) {
                    response.getWriter().write("No file specified.");
                    response.flushBuffer();
                    return;
                }

                if (!contextUser.application().m_cluster_manager.has_enough_space(fileSize)) { // check if user has enough space available
                    response.getWriter().write("File too large");
                    response.flushBuffer();
                    return;
                }

                if (filesystem_file == null) { // new file clicked
                    File look_for_existing = File.get_file_by_path(contextFolder.get_path() + fileName, contextUser);
                    if (look_for_existing != null) { // new file, check if already exists
                        filesystem_file = look_for_existing;
                    } else {
                        filesystem_file = new File(contextFolder, fileName);
                    }
                }
                // now we have the rights and are able to upload

                // create new FileVersion
                FileVersion old_version = filesystem_file.get_current_version();
                FileVersion new_version = new FileVersion(filesystem_file);

                // get cache target
                Location cache = Location.get_cache_location_for_size(fileSize);
                String cachepath = cache.get_path();
                String cachefilepath = cachepath + "/" + new_version.get_id() + ".tmp";
                java.io.File file = new java.io.File(cachefilepath);

                // lock cache file
                if (CacheFileLock.getInstance().isLocked(new_version.get_id()) != null) {
                    response.getWriter().write("Internal Server Error");
                    response.flushBuffer();
                    throw new IOException(new_version + " is locked.");
                } else {
                    CacheFileLock.getInstance().lock(new_version.get_id(), cachefilepath);
                    Files.deleteIfExists(Paths.get(cachefilepath));
                }


                if (!file.createNewFile()) { // real write in cache,
                    response.getWriter().write("The file already exists in repository.");
                    response.flushBuffer();
                    throw new IOException("The file already exists in repository.");
                }

                upload_formdata.write(file); // then write file to cachefilepath
                java.io.File cachefile = new java.io.File(cachefilepath);
                new_version.set_size(cachefile.length());
                new_version.set_last_modified(cachefile.lastModified());
                new_version.set_checksum(Location.file_checksum(cachefile));
                filesystem_file.set_real_file(cachefile);

                Set<StorageCell> locationset = Location.make_file_persistent(cachefile, new_version); // write file in backends
                if (locationset.size() == 0) {
                    LOGGER.warning("Failed to upload fileversion: " + new_version + " from " + cachefile);
                    response.setContentType("text/xml");
                    response.getWriter().write("Backend error");
                    response.flushBuffer();
                    throw new IOException("Failed to upload fileversion: " + new_version + " from " + cachefile);
                } else {
                    new_version.set_locationset(locationset);
                    CacheFileLock.getInstance().release(new_version);

                    filesystem_file.set_current_version(new_version);
                    filesystem_file.set_real_file(null);
                    filesystem_file.set_persistent(true);
                    new_version.set_previous(old_version);

                    filesystem_file.db_update();
                    new_version.db_sync();


                    // response: fileupload success
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.getWriter().write("success");
                    response.flushBuffer();
                }

            } catch (FileUploadException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while creating the file : " + e.getMessage());
            } catch (Exception _e) {
                _e.printStackTrace();
            }
        } else {
            super.service(request, response);
        }
    }

    /**
     * Handel the GET request for filedownload
     * creates an response for requerd file
     *
     * @param req  incoming request
     * @param resp response for the request (retirns filestream)
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        int file_id = req.getParameter("file") != null && !req.getParameter("file").isEmpty()  ?  Integer.parseInt(req.getParameter("file")): -1;
        int file_version_id = req.getParameter("fileversion") != null && !req.getParameter("fileversion").isEmpty()? Integer.parseInt(req.getParameter("fileversion")): -1;
        User contextUser = null;
        HttpSession session = req.getSession();
        Object userObj = session.getAttribute("user");  // get user from session
        if (userObj != null && userObj instanceof User) {
            contextUser = (User) userObj;
        }
        FileVersion version = null;
        if(file_id > -1) {
            version = File.find(file_id).get_current_version();
        }else if (file_version_id > -1){
            version = FileVersion.find(file_version_id);
        }
        if (contextUser != null && version != null) {
            ServletOutputStream os = resp.getOutputStream();
            ServletContext context = getServletConfig().getServletContext();
            String type = context.getMimeType(version.get_file().get_title());
            // setup response
            resp.setContentLength((int) (version.get_size()));
            resp.setContentType(type != null ? type : "application/octet-stream");
            resp.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", version.get_file().get_title()));

            java.io.File real = Location.stage_file_to_cache(version);

            if (real != null && CacheFileLock.getInstance().isReadable(version)) {
                Streams.copy(new FileInputStream(real), os, true); // write file to output
                CacheFileLock.getInstance().release(version);
            } else {
                resp.getWriter().write("upload failed");
                resp.flushBuffer();
            }
        } else if (contextUser == null) {
            resp.getWriter().write("not logedin");
            resp.flushBuffer();
        } else if (version == null) {
            LOGGER.warning("client '" + contextUser + "' requested not existent fileversionid: " + file_version_id );
            resp.getWriter().write("Internal Error");
            resp.flushBuffer();
        }
    }

    @Override
    public String login(String name, String password) {
        if (User.authenticate(name, password)) {
            User user = User.find_by_name(name);
            this.storeUserInSession(user);
            return user.toJson();
        } else {
            return "Error : No user find with the name \"" + name + "\"";
        }
    }

    @Override
    public String loginFromSession() {
        return this.getUserAlreadyFromSession().toJson();
    }

    @Override
    public void logout() {
        this.deleteUserFromSession();
    }

    @Override
    public String getFolderById(int folder_id) {
        User user = this.getUserAlreadyFromSession();
        FileFolder folder = FileFolder.find(folder_id);
        if (folder.get_user() == user) {
            return folder.toJson(user.get_id());
        }
        return null;
    }

    @Override
    public String getFileById(int file_id) {
        User user = this.getUserAlreadyFromSession();
        File file = File.find(file_id);
        if (user == file.get_user()) {
            return file.toJson(user.get_id());
        }
        return null;
    }

    @Override
    public String renameFolder(int folder_id, String newTitle) {
        User user = this.getUserAlreadyFromSession();
        FileFolder folder = FileFolder.find(folder_id);
        if (user == folder.get_user()) {
            folder.set_title(newTitle);
            folder.db_update();
            return folder.get_parent().toJson(user.get_id());
        }
        return null;
    }

    @Override
    public String renameFile(int file_id, String newTitle) {
        User user = this.getUserAlreadyFromSession();
        File file = File.find(file_id);
        if (user == file.get_user()) {
            file.set_title(newTitle);
            file.db_update();
            return file.get_parent().toJson(user.get_id());
        }
        return null;
    }

    @Override
    public String createFolder(int app_id, int parent_id, String title) {
        FileFolder ff = new FileFolder(FileFolder.find(parent_id), title);
        return ff.get_parent().toJson(this.getUserAlreadyFromSession().get_id());
    }

    @Override
    public String deleteFolder(int folder_id) {
        FileFolder folder = FileFolder.find(folder_id);
        FileFolder parent = folder.get_parent();
        folder.delete();
        return parent.toJson(this.getUserAlreadyFromSession().get_id());
    }

    @Override
    public String deleteFile(int file_id) {
        File file = File.find(file_id);
        FileFolder parent = file.get_parent();
        file.delete();
        return parent.toJson(this.getUserAlreadyFromSession().get_id());
    }

    @Override
    public String getFileVersionList(int file_id) {
        return File.find(file_id).get_current_version().toJsonList();
    }

    @Override
    public String rollbackToVersion(int version_id) {
        FileVersion version = FileVersion.find(version_id);
        if (version != null){
            File file = version.get_file();
            file.test_rollback_file(version_id);
            return file.get_current_version().toJsonList();
        }
        return null;
    }

    private User getUserAlreadyFromSession() {
        User user = null;
        HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
        HttpSession session = httpServletRequest.getSession();
        Object userObj = session.getAttribute("user");
        if (userObj != null && userObj instanceof User) {
            user = (User) userObj;
        }
        return user;
    }

    private void storeUserInSession(User user) {
        HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
        HttpSession session = httpServletRequest.getSession(true);
        user.set_session_id(session.getId());
        session.setAttribute("user", user);
    }

    private void deleteUserFromSession() {
        HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
        HttpSession session = httpServletRequest.getSession();
        session.removeAttribute("user");
    }
}
