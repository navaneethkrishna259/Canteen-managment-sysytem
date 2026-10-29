
package canteen;

public class Admin extends User {

    public Admin(int userId, String username, String password) {
        super(userId, username, password);
    }

    @Override
    public double calculateDiscount(double total) {
        return 0.0;
    }
}
