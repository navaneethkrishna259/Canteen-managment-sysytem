
package canteen;

public class MenuItem {

    private int itemId;
    private String itemName;
    private double price;
    private int stock;

    public MenuItem(int itemId, String itemName, double price, int stock) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.stock = stock;
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price >= 0) {
            this.price = price;
        }
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock >= 0) {
            this.stock = stock;
        }
    }

    @Override
    public String toString() {
        return itemName + " - Rs. " + price + " (Stock: " + stock + ")";
    }
}
