package com.github.lyokofirelyte.Ataxia.data;

public enum Role {

	OVERSEER("199675551961710593", 5),
	DEVELOPER("204056639865618432", 4),
	ADMIN("199663706647429120", 3),
	MOD("199663792613752843", 2),
	MEMBER("199663852630179840", 1),
	GUEST("209515879585677312", 0);
	
	Role(String id, int rank){
		this.id = id;
		this.rank = rank;
	}
	
	private String id;
	private int rank = 0;
	
	public String getId(){
		return id;
	}
	
	public int getRank(){
		return rank;
	}
	
	public boolean hasPermission(String id){
		try {
			return value(id.replace("@", "").replace("&", "")).getRank() >= getRank();
		} catch (Exception e){
			return false;
		}
	}
	
	public Role value(String id){
		for (Role r : Role.values()){
			if (r.getId().equals(id)){
				return r;
			}
		}
		return null;
	}
}