
public class Team {

	private String id1;
	private String id2;
	private String state1;
	private String state2;
	
	
	public Team(String id1, String id2) {
		super();
		this.id1 = id1;
		this.id2 = id2;
	}
	
	public String Result(String s1, String s2)
	{
		if(s1.equals("win")&&s2.equals("win"))
			return "win";
		else if(s1.equals("lost")&&s2.equals("lost"))
			return "lost";
		else if(s1.equals("tie")&&s2.equals("lost"))
			return "lost";
		else if(s1.equals("lost")&&s2.equals("tie"))
			return "lost";
		else if(s1.equals("win")&&s2.equals("tie"))
			return "win";
		else if(s1.equals("tie")&&s2.equals("win"))
			return "win";
		else if(s1.equals("tie")&&s2.equals("tie"))
			return "tie";
		else if(s1.equals("lost")&&s2.equals("win"))
			return "tie";
		else
			return"tie";
		
	}
	
	public String getId1() {
		return id1;
	}

	public void setId1(String id1) {
		this.id1 = id1;
	}

	public String getId2() {
		return id2;
}

	public void setId2(String id2) {
		this.id2 = id2;
	}



	public String getState1() {
		return state1;
	}
	public void setState1(String state1) {
		this.state1 = state1;
	}
	public String getState2() {
		return state2;
	}
	public void setState2(String state2) {
		this.state2 = state2;
	}
	
	


}
