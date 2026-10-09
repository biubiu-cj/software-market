package software_market.service;

public interface MailService {
    void sendCode(String email, String code);
}
