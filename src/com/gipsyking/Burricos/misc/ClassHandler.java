package com.gipsyking.Burricos.misc;

import org.bukkit.Server;

import com.gipsyking.Burricos.NMSWrapperInterface;


/*
 * Thank you rourke for showing me how to do this.
 */
public class ClassHandler {

	public static ClassHandler ch;
	private String version;
	
	public static boolean Initialize(Server server){
		ch = new ClassHandler();
		String packageName = server.getClass().getPackage().getName();
		ch.version = packageName.substring(packageName.lastIndexOf('.') + 1);
		try {
			Class.forName("com.gipsyking.Burricos.misc." + ch.version + ".NMSWrapper");
			return true;
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	private Object getObject(Class<? extends Object> Class, String name){
		try {
			Class<?> internalClass = Class.forName("com.gipsyking.Burricos.misc." + ch.version + "." + name);
			if (internalClass.isAssignableFrom(internalClass)) 
				return internalClass.getConstructor().newInstance();
		} catch (Exception e) {
            e.printStackTrace();
		}
		return null;
	}

	public NMSWrapperInterface getNMSWrapper() {
		return (NMSWrapperInterface) getObject(NMSWrapperInterface.class, "NMSWrapper");
	}
}