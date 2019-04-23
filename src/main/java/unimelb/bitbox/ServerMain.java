package unimelb.bitbox;

import static org.junit.Assume.assumeNoException;

import java.io.FileDescriptor;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.FileSystemObserver;
import unimelb.bitbox.util.FileSystemManager.EVENT;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;

public class ServerMain implements FileSystemObserver {
	private static Logger log = Logger.getLogger(ServerMain.class.getName());
	protected FileSystemManager fileSystemManager;

	public ServerMain() throws NumberFormatException, IOException, NoSuchAlgorithmException {
		fileSystemManager = new FileSystemManager(Configuration.getConfigurationValue("path"), this);
	}

	@Override
	public void processFileSystemEvent(FileSystemEvent fileSystemEvent) {
		EVENT event = fileSystemEvent.event;

		switch (event) {
		case FILE_CREATE:
			JSONObject json = getFileRequestFormat(fileSystemEvent);
			try {
				ConnectionHost.sendAll(json);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// send to the peers
			break;
		case FILE_MODIFY:
			getFileRequestFormat(fileSystemEvent);
			break;
		case FILE_DELETE:
			getFileRequestFormat(fileSystemEvent);
			break;
		case DIRECTORY_CREATE:
			getDirRequestFormat(fileSystemEvent);
			break;
		case DIRECTORY_DELETE:
			getDirRequestFormat(fileSystemEvent);
			break;
		default:
			break;
		}
		// Sending send = new Sending(Peer.getAddress(), Peer.getPortNo(), event);
		// Thread sendThread = new Thread(send);
		// sendThread.start();
	}

	public JSONObject getDirRequestFormat(FileSystemEvent fileSystemEvent) {
		/**
		 * DIRECTORY_CREATE_REQUEST, DIRECTORY_DELETE_REQUEST
		 */
		JSONObject json = new JSONObject();
		json.put("command", fileSystemEvent.event.toString() + "_REQUEST");
		json.put("pathName", fileSystemEvent.path);

		System.out.println(json.toString());
		return json;
	}

	public JSONObject getFileRequestFormat(FileSystemEvent fileSystemEvent) {
		/**
		 * FILE_CREATE_REQUEST, FILE_DELETE_REQUEST, FILE_MODIFY_REQUEST
		 */
		JSONObject json = new JSONObject();
		JSONObject des = new JSONObject();
		des.put("md5", fileSystemEvent.fileDescriptor.md5);
		des.put("lastModified", fileSystemEvent.fileDescriptor.lastModified);
		des.put("fileSize", fileSystemEvent.fileDescriptor.fileSize);

		json.put("fileDescriptor", des);
		json.put("command", fileSystemEvent.event.toString() + "_REQUEST");
		json.put("pathName", fileSystemEvent.path);

		System.out.println(json.toString());
		return json;
	}

	public JSONObject fileCreateResponse(JSONObject request) throws NoSuchAlgorithmException, IOException {
		//send to the client
		JSONObject json = new JSONObject();
		JSONObject description = new JSONObject();

		//resolving
		JSONObject des = (JSONObject) request.get("fileDescriptor");
		String name = (String) request.get("pathName");
		String md5 = (String) des.get("md5");
		long lm = (long) des.get("lastModified");
		long size = (long) des.get("fileSize");

		description.put("md5", md5);
		description.put("lastModified", lm);
		description.put("fileSize", size);
		json.put("command", "FILE_CREATE_RESPONSE");
		json.put("pathName", name);

		boolean ready = false;
		if (fileSystemManager.isSafePathName(name)) {
			if (!fileSystemManager.fileNameExists(name)) {
				if (fileSystemManager.createFileLoader(name, md5, size, lm)) {
					json.put("message", "file loader ready");
					ready = true;
				} else {
					json.put("message", "file loader notready");
				}
			} else {
				json.put("message", "pathname already exists");
			}
			
		} else {
			json.put("message", "unsafe pathname given");
		}
		json.put("status", ready);


		// send to the client
		return json;
	}
}
