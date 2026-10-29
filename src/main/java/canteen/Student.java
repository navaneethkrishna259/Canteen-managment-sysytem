
package canteen;

public class Student extends User {

    public Student(int userId, String username, String password) {
        super(userId, username, password);
    }

    @Override
    public double calculateDiscount(double total) {
        if (total <= 0) {
            return 0;
        }

        return total * 0.05;
    }
}
