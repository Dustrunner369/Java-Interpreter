public class Register {

    private String type;
    private Object value;

    public Register(String type) {
        this.type = type;
        if (type.equals("float") || type.equals("bool")) {
            this.value = 0.0;
        } else if (type.equals("string")) {
            this.value = "UNDEFINED";
        } else if (type.equals("vector3")) {
            this.value = null;
        }
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
