
public class ResultTables {
	String array[][] = new String[4][2];

	public ResultTables(String[][] array) {
		super();
		this.array = array;
	}

	public String getElement(int x, int y) {
		return array[x][y];
	}

	public void setArray(int x, int y, String msg) {
		array[x][y] = msg;
	}
	

}
