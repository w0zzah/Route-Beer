package seng202.team5.services;

import seng202.team5.models.User;
import seng202.team5.models.types.UserType;

/**
 * Account logic for the current user so the user does not
 */
public class AccountService {

    private final MainManager mainManager;

    /**
     * Account service initialiser
     * @param mainManager the app main's mainManager
     */
    public AccountService(MainManager mainManager) {
        this.mainManager = mainManager;
    }

    /**
     * Getter which fetches the current user of the app
     * @return the current user of the application
     */
    public User getCurrentUser() {
        return mainManager.getCurrentUser();
    }

    /**
     *
     * @return boolean; true if the user is a guest, false if not
     */
    public boolean isGuest() {
        return getCurrentUser().getType() == UserType.GUEST;
    }

    /**
     * Deletes the current user's account
     */
    public void deleteAccount() {
        mainManager.deleteUser();
    }
}