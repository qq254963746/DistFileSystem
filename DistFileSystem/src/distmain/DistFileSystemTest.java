/**
 * @author paul
 */

package distmain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import distclient.Client;
import distclient.ClntGetFile;
import distclient.ClntUploadFile;
import distfilelisting.FileObject;
import distfilelisting.LocalPathList;
import distfilelisting.UserManagement;
import distnodelisting.NodeSearchTable;

public class DistFileSystemTest {
	private UserManagement userManage = null;
	private NodeSearchTable nst = null;
	private BufferedReader inStream;
	
	public DistFileSystemTest () {
		this.userManage = UserManagement.get_Instance();
		this.nst = NodeSearchTable.get_Instance();
		this.inStream = new BufferedReader(new InputStreamReader(System.in));
	}
	
	public Thread runTestCommand (String command) {
		try {
			if (command.contains("addfile")) {
				System.out.printf("Path to File: ");
				String pathToFile = inStream.readLine().trim();
				File fi = new File(pathToFile);
				if (!fi.exists()) {
					FileNotFoundException fnfe = 
							new FileNotFoundException(pathToFile);
					throw fnfe;
				}
				int lastSlash = pathToFile.lastIndexOf(System.getProperty("file.separator"));
				String filename = pathToFile.substring(lastSlash+1);
				
				System.out.printf("File Permissions (744): ");
				String permissions = inStream.readLine();
				
				FileObject nfo = new FileObject(filename, permissions, 
						this.userManage.get_ownUserName(), this.userManage.get_ownUserName());
				
				Client cli = new Client();
				ClntUploadFile cuf = new ClntUploadFile (cli, nfo, fi, this.userManage.get_ownUserName());
				cuf.run();
				//cli.addTask(cuf);	
			}
			
			else if (command.contains("view files")) {
				LocalPathList lpl = LocalPathList.get_Instance();
				System.out.printf("Number of Files : %d\n", lpl.size());
				for (int index = 0; index < lpl.size(); index++) {
					System.out.printf("File Number: %s\tFile Name: %s\n", index, lpl.get(index).getName());
				}
			}
			
			else if (command.contains("view users")) {
				UserManagement um = UserManagement.get_Instance();
				Hashtable<String, Vector<String>> users = um.get_usernames();
				Enumeration<String> keys = users.keys();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					System.out.printf("%s: ", key);
					for (String elm : users.get(key)) {
						System.out.printf("%s, ", elm);
					}
					System.out.print("\n");
				}
			}
			
			else if (command.contains("getfile")) {
				System.out.printf("File Name: ");
				String fileName = inStream.readLine();
				System.out.printf("Path to Place: ");
				String pathToPlace = inStream.readLine();
				
				Client cli = new Client();
				
				ClntGetFile cgf = new ClntGetFile (cli, fileName, pathToPlace, this.userManage.get_ownUserName());
				cgf.run();
			}
		}
		catch (FileNotFoundException e) {
			System.out.printf("The file %s, could not be found\n", e.getMessage());
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
