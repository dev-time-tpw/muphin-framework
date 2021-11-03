package de.devtime.muphin.examples.ex1;

import java.io.IOException;

import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import de.devtime.utils.resources.ConfigManager;

/**
 * Helper class to start all the needed systems to run the examples.
 *
 * @author morrigan
 * @since 0.0.1
 */
public class StartupFakes {

  private static final ConfigManager CONFIGS = ConfigManager.getInstance();

  /**
   * Start all necessary systems for example 1.
   *
   * @throws IOException
   * @since 0.0.1
   */
  public static final void startupEx1() throws IOException {
    CONFIGS.loadAllConfigsFromResources("ex1/ex1_configs.properties");

    startFakeFtpServer(CONFIGS.getConfig("username"), CONFIGS.getConfig("userpw"), CONFIGS.getConfig("userhome"));
    FakeFtpServer fakeFtpServer = new FakeFtpServer();
    fakeFtpServer.addUserAccount(
        new UserAccount(CONFIGS.getConfig("username"), CONFIGS.getConfig("userpw"), CONFIGS.getConfig("userhome")));

    FileSystem filesystem = new UnixFakeFileSystem();
    filesystem.add(new DirectoryEntry("/home/test/data"));

  }

  // checkstyle:WriteTag OFF

  private static final void startFakeFtpServer(String username, String password, String homedir) {
    FakeFtpServer fakeFtpServer = new FakeFtpServer();
    fakeFtpServer.addUserAccount(new UserAccount(username, password, homedir));

    FileSystem filesystem = new UnixFakeFileSystem();
    filesystem.add(new DirectoryEntry("/home/test/data"));
  }

  private StartupFakes() {
    super();
  }

  // checkstyle:WriteTag ON
}
