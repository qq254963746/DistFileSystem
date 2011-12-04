/**
 * @author paul
 */

package distmain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

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
				fi = null;
				int lastSlash = pathToFile.lastIndexOf(System.getProperty("file.separator"));
				String filename = pathToFile.substring(lastSlash+1);
				
				System.out.printf("File Permissions (744): ");
				String permissions = inStream.readLine();
				
				FileObject nfo = new FileObject(filename, permissions, 
						this.userManage.get_ownUserName(), this.userManage.get_ownUserName());
				
				Client cli = new Client();
				ClntUploadFile cuf = new ClntUploadFile (cli, nfo, pathToFile, this.userManage.get_ownUserName());
				cli.addTask(cuf);	
			}
			
			else if (command.contains("view files")) {
				LocalPathList lpl = LocalPathList.get_Instance();
				System.out.printf("Number of Files : %d\n", lpl.size());
				for (int index = 0; index < lpl.size(); index++) {
					System.out.printf("File Number: %s\tFile Name: %s\n", index, lpl.get(index).getName());
				}
			}
			
			else if (command.contains("get file")) {
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
