package com.github.lyokofirelyte.Ataxia.message;

public enum Role {

	DEVELOPER("204056639865618432", 3),
	ADMIN("199663706647429120", 2),
	MOD("199663792613752843", 1),
	MEMBER("199663852630179840", 0);
	
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