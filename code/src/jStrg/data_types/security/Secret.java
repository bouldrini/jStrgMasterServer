package jStrg.data_types.security;

import jStrg.file_system.Settings;

import java.util.Base64;

/**
 * used to pass a password between functions
 */
public class Secret {

    private byte[] m_key;
    private byte[] m_salt;

    private String m_encoded_key;
    private String m_encoded_salt;

    public String key_encoded() {
        if (m_encoded_key == null) {
            m_encoded_key = Base64.getEncoder().encodeToString(m_key);
        }
        return m_encoded_key;
    }

    public String salt_encoded() {
        if (m_encoded_salt == null) {
            m_encoded_salt = Base64.getEncoder().encodeToString(m_salt);
        }
        return m_encoded_salt;
    }

    public byte[] key() {
        if (m_key == null) {
            m_key = new byte[Settings.m_password_key_length];
            m_key = Base64.getDecoder().decode(m_encoded_key);
        }
        return m_key;
    }

    public byte[] salt() {
        if (m_salt == null) {
            m_salt = new byte[Settings.m_password_salt_length];
            m_salt = Base64.getDecoder().decode(m_encoded_salt);
        }
        return m_salt;
    }

    // setter

    public void set_encoded_salt(String m_encoded_salt) {
        this.m_encoded_salt = m_encoded_salt;
    }

    public void set_key(byte[] m_key) {
        this.m_key = m_key;
    }

    public void set_salt(byte[] m_salt) {
        this.m_salt = m_salt;
    }

    public void set_encoded_key(String m_encoded_key) {
        this.m_encoded_key = m_encoded_key;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("<{");

        if (m_encoded_key != null) {
            output.append(" key: " + m_encoded_key);
        }

        if (m_encoded_salt != null) {
            output.append(" salt: " + m_encoded_salt);
        }

        output.append(" }>");
        return output.toString();
    }
}
