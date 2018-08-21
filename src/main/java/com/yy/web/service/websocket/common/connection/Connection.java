package com.yy.web.service.websocket.common.connection;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.websocket.Session;


/**
 * 常规数据连接套接字的会话连接管理。
 * 
 * @since 2017-10-26
 * @version 1.0
 * @author Luowen
 */
public class Connection {


	/**
	 * 存储所有接入的套接字客户端会话。
	 * 
	 * @key sessionId
	 * @value 套接字结构对象。
	 */
	private static Map<String, ConnectionStruct> connectionMap = new Hashtable<>();
	
	
	/**
	 * 获取所有可用的会话编号列表。
	 * 
	 * @return
	 */
	public static List<String> getSessionIds() {
		
		List<String> ids = new ArrayList<>();
		Iterator<String> sessionIds = connectionMap.keySet().iterator();


		while (sessionIds.hasNext()) {
			ids.add(sessionIds.next());
		}
		
		
		return ids;
	}


	/**
	 * 将某套接字会话添加到会话集中。
	 * 
	 * @param session
	 */
	public static void addSession(Session session) {

		String sessionId = session.getId();

		ConnectionStruct sessionInfo = new ConnectionStruct();
		sessionInfo.setSessionId(sessionId);
		sessionInfo.setSession(session);


		connectionMap.put(sessionId, sessionInfo);
	}
	

	/**
	 * 从会话集中移除某套接字会话。
	 * 
	 * @param session
	 */
	public static void removeSession(Session session) {
		
		connectionMap.remove(session.getId());
	}


	/**
	 * 根据会话编号，获取会话信息类。
	 * 
	 * @param sessionId
	 * @return
	 */
	public static ConnectionStruct getSessionInfo(String sessionId) {

		return connectionMap.get(sessionId);
	}
	
	
	/**
	 * 获取指定 key 的所有连接会话信息。
	 * 
	 * @param key
	 * @return
	 */
	public static List<ConnectionStruct> getKeySessionInfo(String key) {
		
		List<ConnectionStruct> list = new ArrayList<>();
		Iterator<String> keys = connectionMap.keySet().iterator();


		while (keys.hasNext()) {
			ConnectionStruct sessionInfo = connectionMap.get(keys.next());
			
			if (sessionInfo != null && key.equals(sessionInfo.getKey())) {
				list.add(sessionInfo);
			}
		}
		
		
		return list;
	}
	
	
	/**
	 * 获取指定 key 指定 sign 的会话信息。
	 * 
	 * @param key
	 * @param sign
	 * @return
	 */
	public static ConnectionStruct getKeySessionInfo(String key, String sign) {
		
		List<ConnectionStruct> list = getKeySessionInfo(key);
		for (ConnectionStruct item : list) {
			if (item.getSign().equals(sign)) {
				return item;
			}
		}
		
		
		return null;
	}
}
