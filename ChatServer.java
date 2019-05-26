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
			synchronized(hm){//�� ����� �� �浹�� �������ؼ�
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	/*
	 ������ ��Ű�� ���� �ܾ String arry�� �����Ѵ��� String array�� �޾Ƽ� �񱳸� �ؼ� �ε����� �ִٸ� 
	 prohibiWord�޼ҵ带 �����ϰ� checkMethod�� false�� �ٲ㼭 ���� method���� ���� ���ϰ� �Ѵ�.
	  */
	public void run(){
		try{
			String line = null;
			boolean checkTheMethod=false;
			String[] prohibit={"hello","what","why","how","when","where"};
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
				for(String check:prohibit) {
					checkTheMethod=true;
					if(line.indexOf(check)!= -1) {
						checkTheMethod=false;
						prohibitWord();
						break;
					}
				}
				if(checkTheMethod) {
				if(line.indexOf("/to ") == 0){
					for(String check:prohibit) {
						checkTheMethod=true;
						if(line.indexOf(check) != -1) {
							checkTheMethod=false;
							prohibitWord();
							break;
						}
					}
					if(checkTheMethod) { 
						sendmsg(line);
					}
				}else if(line.equals("/userlist")) {
					send_userlist();
				}else { 
					broadcast(id + " : " + line);
				}
			}
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
	 ���⼭ while������ if���� ����ؼ� ����� 
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
	/*iterator�� �ݺ��Ҷ� �ڱ� �ڽ��� �ƴҶ��� messege�� ������ ����
	�̰͵� ���̵� ���ؼ� ���̵� ���ؼ� printwriter�� ���߰ڴ�. 
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
	 ���� ������ ����� ��� ���� ��� 
	 �ڽ����׸� ������� ���� id�� �����ش�.
	 ����� ���� �ؽ����� ũ�⸦ �̿��ؼ�
	 ����� ���̵�� ��ε� ĳ��Ʈ�� ���� ������� �Ѵ�.
	 ���� ����ڴ� ��� �˼� ������?
	 �����ִ� chatThred�� id�� ������ ����
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
	/*
	 ���� �ܾ ������ �� ���� �ܾ �� ����ڿ��� ������� ����� ������ ��������
	 ����ڴ� send_userlistó�� ã�Ƽ� ������ �ǰڴ�.
	 */
	
	public void prohibitWord() {
		PrintWriter object=(PrintWriter)hm.get(id);
		String msg="don't use that word";
		object.println(msg);
		object.flush();
	}
}	
