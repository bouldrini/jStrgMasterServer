package jStrg.tests;

import jStrg.data_types.exceptions.ConfigException;
import jStrg.file_system.*;
import jStrg.file_system.File;
import jStrg.network_management.core.Server;
import jStrg.network_management.storage_management.config.ILocationConfig;
import jStrg.network_management.storage_management.core.Location;
import jStrg.network_management.storage_management.core.StorageCell;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

public class helper {

    /**
     * checks if testfiles are located where expected
     *
     * @return path
     * @throws IOException -
     */
    public static java.io.File search_testfile_path() throws IOException {
        String testfile_path = null;
        String cwd = System.getProperty("user.dir");
        System.out.println("working dir: " + cwd);
        if (Files.exists(Paths.get(cwd + "/testdata/"))) {
            testfile_path = cwd + "/testdata/";
        }
        if (Files.exists(Paths.get(cwd + "/code/testdata/"))) {
            testfile_path = cwd + "/code/testdata/";
        }
        if (Files.exists(Paths.get(Settings.m_dev_repository_path + "/code/testdata/"))) {
            testfile_path = Settings.m_dev_repository_path + "/code/testdata/";
        }
        System.out.println("Looking for files in " + testfile_path);
        if (testfile_path == null)
            throw new IOException("testfiles not found.");
        return new java.io.File(testfile_path);
    }

    public static LinkedHashMap get_testfilelist(User _user, Application _app) throws IOException {
        LinkedHashMap<String, File> ret = new LinkedHashMap<>();
        java.io.File filelist[] = search_testfile_path().listFiles();
        for (java.io.File file : filelist) {
            File addFile = new File(_user.get_rootfolder(), file.getName());
            addFile.set_real_file(file);
            String checksum = Location.file_checksum(file);
            ret.put(checksum, addFile);
            System.out.println("found file: " + file);
        }
        return ret;
    }

    public static java.io.File testfile_to_cache() {
        java.io.File file = null;
        String location_path = null;
        try {
            java.io.File[] files = search_testfile_path().listFiles();
            file = files[0];
            location_path = Location.get_cache_location_for_size(file.length()).get_path();
            location_path = location_path + "/" + file.getName();
            Files.copy(Paths.get(file.toString()) , Paths.get(location_path));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        return new java.io.File(location_path);
    }

    public static void create_locations(Application _app) throws ConfigException {

        if (Settings.m_dev_use_disk_location) {
            StorageCell my_persistent_location = get_temporary_location(Location.TYPE.DISK, _app);
        }
        if (Settings.m_dev_use_s3_location) {
            StorageCell my_s3_location = get_temporary_location(Location.TYPE.S3, _app);
        }
        if (Settings.m_dev_use_google_location) {
            StorageCell my_google_location = get_temporary_location(Location.TYPE.GOOGLE, _app);
        }
        create_cache_location(_app);
    }

    public static void create_cache_location(Application _app) {
        ILocationConfig diskconfigurator = ILocationConfig.create_configurator();
        diskconfigurator
                .set_path(Settings.m_dev_default_cache_location);
        diskconfigurator.set_application(_app);
        StorageCell my_cache_location = Location.create_location(Location.TYPE.CACHE, diskconfigurator);
    }

    public static StorageCell get_temporary_location(Location.TYPE _type, Application _app) throws ConfigException {
        switch (_type) {
            case GOOGLE:
                ILocationConfig googleconfig = ILocationConfig.create_configurator();
                googleconfig
                        .set_path(Settings.m_dev_default_google_bucket)
                        .google_set_credential_file(Settings.m_dev_default_google_credentials)
                        .google_set_projectid(Settings.m_dev_default_google_projectid)
                        .set_max_usage(Settings.m_dev_bytes_per_google_bucket);
                googleconfig.set_application(_app);
                return _app.m_google_cloud_bucket_cluster.register(googleconfig);
            case DISK:
                ILocationConfig diskconfigurator = ILocationConfig.create_configurator();
                diskconfigurator
                        .set_path(Settings.m_dev_default_disk_location)
                        .set_max_usage(Settings.m_dev_bytes_per_disk);
                diskconfigurator.set_application(_app);
                return _app.m_disk_storage_cluster.register(diskconfigurator);
            case S3:
                ILocationConfig s3configurator = ILocationConfig.create_configurator();
                s3configurator
                        .set_path(Settings.m_dev_default_s3_location)
                        .s3_set_access_id(Settings.m_dev_s3_aws_access_key_id)
                        .s3_set_access_key(Settings.m_dev_s3_aws_access_key)
                        .set_max_usage(Settings.m_dev_bytes_per_s3_bucket);
                s3configurator.set_application(_app);
                return _app.m_amazon_s3_bucket_cluster.register(s3configurator);
            default:
                return null;
        }
    }

    public static void deleteFolder(java.io.File folder) {
        java.io.File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (java.io.File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    public static void createUserFolder() {
        try {
            Path path = Paths.get(Settings.user_folder_root);
            if (Files.exists(path))
                helper.deleteFolder(path.toFile());
            Files.createDirectory(Paths.get(Settings.user_folder_root));

        } catch (IOException e) {
            Settings.LOGGER.severe("failure: " + e);
        }
    }

    public static void clearEnvironment() {

        Privilege.delete_all();

        File.delete_all();
        FileVersion.deleteAll();
        FileFolder.delete_all();
//        Environment.FILE_TYPE_MAPPINGS.clear();
        FileType.delete_all();
        User.delete_all();
        Role.delete_all();
        StorageCell.delete_all();
        Server.delete_all();
        Application.delete_all();
        Settings.delete_all();
    }

    public static void setupStoragePools(User _user) {
        if (Settings.m_dev_use_disk_location) {
            _user.add_storagepool(Location.TYPE.DISK);
        }
        if (Settings.m_dev_use_s3_location) {
            _user.add_storagepool(Location.TYPE.S3);
        }
        if (Settings.m_dev_use_google_location) {
            _user.add_storagepool(Location.TYPE.GOOGLE);
        }
    }

    public static void fill_files(User _user) {
        java.io.File[] testfiles = null;
        int i = 0;
        try {
            testfiles = search_testfile_path().listFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setupStoragePools(_user);
        for ( File file : _user.files()) {
            if (file.get_user() != _user) {
                continue; // exclude privileges
            }
            file.test_import_file(testfiles[i].getAbsolutePath());
            file.set_title(testfiles[i++].getName());
            if (i == testfiles.length) {
                i = 0;
            }
        }
    }

}
