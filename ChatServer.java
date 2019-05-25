import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){//줄 세우는 것 충돌을 막기위해서
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
				if(line.equals("hello")||line.equals("what")||line.equals("why")||line.equals("how")||line.equals("when")||line.equals("where")) {
					prohibitWord();
				}else if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}else if(line.equals("/userlist")) {
					send_userlist();
				}else 
					broadcast(id + " : " + line);
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	/*
	 여기서 while문에서 if문을 사용해서 금지어를 
	  */
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	/*iterator를 반복할때 자기 자신이 아닐때만 messege를 보내면 되지
	이것도 아이디를 비교해서 아이디를 통해서 printwriter를 얻어내야겠다. 
	*/
	public void broadcast(String msg){
		synchronized(hm){
			Collection collection =hm.keySet();
			Iterator iter = collection.iterator();
			while(iter.hasNext()){
				String key = (String)iter.next();
				if(!(id.equals(key))) {
				PrintWriter pw = (PrintWriter)hm.get(key);
				pw.println(msg);
				pw.flush();
				}
			}
		}
	} // broadcast
	/*
	 현재 접속한 사용자 목록 보기 기능 
	 자신한테만 사용자의 수와 id를 보여준다.
	 사용자 수는 해쉬맵의 크기를 이용해서
	 사용자 아이디는 브로드 캐스트와 같은 방식으로 한다.
	 보낸 사용자는 어떻게 알수 있을까?
	 지금있는 chatThred에 id가 적혀져 있지
	 */
	public void send_userlist(){
		Collection collection =hm.keySet();
		Iterator iter = collection.iterator();
		PrintWriter object=(PrintWriter)hm.get(id);
		int size=hm.size();
		object.println(size);
		object.flush();
		while(iter.hasNext()) {
			String key = (String)iter.next();
			object.println(key);
			object.flush();
		}
		
	}
	public void prohibitWord() {
		PrintWriter object=(PrintWriter)hm.get(id);
		String msg="don't use that word";
		object.println(msg);
		object.flush();
	}
}	
