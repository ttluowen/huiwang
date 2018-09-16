package com.yy.web.service.websocket.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.alibaba.fastjson.JSONObject;
import com.yy.callback.CallbackInterface;
import com.yy.log.Logger;
import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeTypeMap;
import com.yy.util.StringUtil;
import com.yy.web.service.websocket.Actions;
import com.yy.web.service.websocket.common.connection.Connection;
import com.yy.web.service.websocket.common.connection.ConnectionStruct;
import com.yy.websocket.streamdata.StreamData;
import com.yy.websocket.streamdata.StreamDataStruct;

/**
 * WebSocket常规数据传递专用通道。
 * 用于非严格验证的一般场景，仅用于数据的快速传递。
 * 
 * @since 2018-02-14
 * @version 1.0
 * @author Luowen
 */
@ServerEndpoint("/webSocket/common")
public class CommonWebSocket {
	
	/**
	 * 当前套接字的访问路径。
	 */
	private static final String WEBSOCKET_PATH = "/webSocket/common";
	
	
	/**
	 * 收到消息时的回调队列列表。
	 * 
	 * @key [sessionId、key、key-sign]
	 * @value CallbackInterface 
	 */
	private static Map<String, CallbackInterface> onMessageCallbacks = new HashMap<>();
	/**
	 * 会话关闭时的回调队列列表。
	 * 
	 * @key [sessionId、key、key-sign]
	 * @value CallbackInterface 
	 */
	private static Map<String, CallbackInterface> onCloseCallbacks = new HashMap<>();


	/**
	 * 当有新套接字接入时的回调。
	 * 
	 * @param session
	 */
	@OnOpen
	public void onOpen(Session session) {

		Logger.log(WEBSOCKET_PATH + " 新接入 session[" + session.getId() + "]");


		// 添加当前会话到队列。
		Connection.addSession(session);
	}

	
	/**
	 * 当收到某套接字客户端发来的消息时的回调。
	 * 
	 * @param session
	 * @param message
	 */
	@OnMessage
	public void onMessage(Session session, String message) {

		StreamDataStruct streamData = StreamData.parse(message);

		if (streamData != null) {
			// 解析成功。
			Logger.log(WEBSOCKET_PATH + " 收到 session[" + session.getId() + "] 消息，" + message);

			actionDo(session, streamData.getAction(), streamData.getData());
		} else {
			Logger.log(WEBSOCKET_PATH + " 收到 session[" + session.getId() + "] 消息，但消息内容无效或格式不正确，" + message);
		}
	}


	/**
	 * 当与某套接字客户端发生异常时的回调。
	 * 
	 * @param session
	 * @param throwable
	 */
	@OnError
	public void onError(Session session, Throwable throwable) {

		Logger.log(WEBSOCKET_PATH + " session[" + session.getId() + "] 异常，" + throwable.getMessage());


		onClose(session);
	}

	
	/**
	 * 当关闭某套接字客户时的回调。
	 * 
	 * @param session
	 */
	@OnClose
	public void onClose(Session session) {

		Logger.log(WEBSOCKET_PATH + " session[" + session.getId() + "] 关闭");

		
		onCallback(session, onCloseCallbacks, Connection.getSessionInfo(session.getId()));
		

		// 移除会话。
		Connection.removeSession(session);
	}
	
	
	/**
	 * 收到消息后的动作处理，并会路由实例类。
	 * 
	 * @param session 会话。
	 * @param action 动作名。
	 * @param data 数据。
	 */
	private void actionDo(Session session, String action, Object data) {

		if (action.equals(Actions.Receive.REGIST)) {
			// 身份注册。
			regist(session, data);
		} else {
			onCallback(session, onMessageCallbacks, Connection.getSessionInfo(session.getId()), action, data);
		}
	}
	
	
	/**
	 * 客户端注册身份。
	 * 
	 * 数据示例：
	 * {"action":"regist","data":{"key":"SGSFStCY"}}
	 * 
	 * @return
	 */
	private void regist(Session session, Object receivedData) {

		StatuscodeTypeMap<Boolean> sm = new StatuscodeTypeMap<Boolean>();


		JSONObject json = (JSONObject) receivedData;

		// 获取身份参数。
		String key = json.getString("key");
		String sign = StringUtil.unNull(json.getString("sign"));
		String sessionId = session.getId();


		// 获取指定的会话。
		ConnectionStruct sessionInfo = Connection.getSessionInfo(sessionId);

		// 将身份信息保存到会话信息中。
		sessionInfo.setKey(key);
		sessionInfo.setSign(sign);


		// 设置返回数据。
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(true);

		
		Logger.log(WEBSOCKET_PATH +  " session[" + session.getId() + "] 成功注册为 key[" + key + "]sign[" + sign + "]");


		// 发给客户端。
		send(sessionId, Actions.Send.REGIST_BACK, sm.toMap());
	}
	
	
	/**
	 * 向指定连接的套接字发送数据。
	 * 
	 * @param sessionId 会话编号。
	 * @param action 动作名。
	 * @param data 数据。
	 */
	public static void send(String sessionId, String action, Object data) {

		// 转换成 JSON 字符串。
		String jsonStr = StreamData.build(action, data).toJson();


		Logger.log(WEBSOCKET_PATH + " session[" + sessionId + "] 发送消息，" + jsonStr);


		try {
			// 获取指定会话，然后发送对象数据（客户端收到的都是字符串的）。
			Connection.getSessionInfo(sessionId).getSession().getBasicRemote().sendText(jsonStr);
		} catch (IOException e) {
			Logger.printStackTrace(e);
		}
	}
	
	
	/**
	 * 向指定连接的套接字发送数据。
	 * 
	 * @param key 使用 key 注册的会话用户。
	 * @param action 动作名。
	 * @param data 数据。
	 */
	public static void sendToKeySession(String key, String action, Object data) {
		
		List<ConnectionStruct> sessions = Connection.getKeySessionInfo(key);
		
		if (sessions != null) {
			for (ConnectionStruct session : sessions) {
				// 转换成 JSON 字符串。
				String jsonStr = StreamData.build(action, data).toJson();
				String sessionId = session.getSessionId();


				Logger.log(WEBSOCKET_PATH + " session[" + sessionId + "] 发送消息，" + jsonStr);


				try {
					// 获取指定会话，然后发送对象数据（客户端收到的都是字符串的）。
					session.getSession().getBasicRemote().sendText(jsonStr);
				} catch (IOException e) {
					Logger.printStackTrace(e);
				}
			}
		}
	}
	
	
	/**
	 * 向指定连接的套接字发送数据。
	 * 
	 * @param key 使用 key 注册的会话用户。
	 * @param sign 唯一标记的前端用户。
	 * @param action 动作名。
	 * @param data 数据。
	 */
	public static void sendToKeySession(String key, String sign, String action, Object data) {
		
		ConnectionStruct session = Connection.getKeySessionInfo(key, sign);

		if (session != null) {
			// 转换成 JSON 字符串。
			String jsonStr = StreamData.build(action, data).toJson();
			String sessionId = session.getSessionId();


			Logger.log(WEBSOCKET_PATH + " session[" + sessionId + "] 发送消息，" + jsonStr);


			try {
				// 获取指定会话，然后发送对象数据（客户端收到的都是字符串的）。
				session.getSession().getBasicRemote().sendText(jsonStr);
			} catch (IOException e) {
				Logger.printStackTrace(e);
			}
		}
	}
	
	
	/**
	 * 添加收到数据时的回调。
	 * 不包括注册。
	 * 
	 * @param key
	 * @param callback
	 */
	public static void addOnMessageCallback(String key, CallbackInterface callback) {
		
		onMessageCallbacks.put(key, callback);
	}
	
	
	/**
	 * 添加关闭时的回调。
	 * 
	 * @param key
	 * @param callback
	 */
	public static void addOnCloseCallback(String key, CallbackInterface callback) {
		
		onCloseCallbacks.put(key, callback);
	}
	
	
	/**
	 * 删除收到数据时的回调。
	 * 
	 * @param key
	 * @return
	 */
	public static CallbackInterface removeOnMessageCallback(String key) {
		
		return onMessageCallbacks.remove(key);
	}
	
	
	/**
	 * 删除关闭时的回调。
	 * 
	 * @param key
	 */
	public static CallbackInterface removeOnCloseCallback(String key) {
		
		return onCloseCallbacks.remove(key);
	}
	
	
	/**
	 * 事件派发处理。
	 * 先使用会话编号来尝试获取回调，如果失败再通过 key+sign 的精确查找，再找不到就使用 key。
	 * 
	 * @param session
	 * @param callbacks
	 * @param params
	 */
	private void onCallback(Session session, Map<String, CallbackInterface> callbacks, Object... params) {

		String sessionId = session.getId();
		CallbackInterface callback = callbacks.get(sessionId + "");
		
		if (callback == null) {
			ConnectionStruct info = Connection.getSessionInfo(sessionId);
			if (info != null) {
				String key = info.getKey();
				String sign = info.getSign();
				String callbacksKey = buildCallbacksKey(key, sign);

				callback = callbacks.get(callbacksKey);
				if (callback == null) {
					callback = callbacks.get(key);
				}
			}
		}


		if (callback != null) {
			callback.todo(params);
		}
	}
	
	
	/**
	 * 构建回调句柄的队列键名。
	 * 
	 * @param key
	 * @param sign
	 * @return
	 */
	public static String buildCallbacksKey(String key, String sign) {
		
		return key + "-" + sign;
	}
}