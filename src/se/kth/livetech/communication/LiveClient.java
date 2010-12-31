package se.kth.livetech.communication;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.thrift.transport.TTransportException;

import se.kth.livetech.communication.thrift.ContestId;
import se.kth.livetech.communication.thrift.LiveService;
import se.kth.livetech.communication.thrift.NodeId;
import se.kth.livetech.contest.model.ContestUpdateListener;
import se.kth.livetech.contest.model.impl.ContestImpl;
import se.kth.livetech.contest.model.test.FakeContest;
import se.kth.livetech.contest.model.test.TestContest;
import se.kth.livetech.contest.replay.ContestReplayer;
import se.kth.livetech.contest.replay.KattisClient;
import se.kth.livetech.contest.replay.LogListener;
import se.kth.livetech.contest.replay.LogSpeaker;
import se.kth.livetech.control.ContestReplayControl;
import se.kth.livetech.control.ui.ProductionFrame;
import se.kth.livetech.presentation.layout.JudgeQueueTest;
import se.kth.livetech.presentation.layout.LivePresentation;
import se.kth.livetech.presentation.layout.ScoreboardPresentation;
import se.kth.livetech.presentation.layout.VLCView;
import se.kth.livetech.presentation.layout.VNCPresentation;
import se.kth.livetech.properties.IProperty;
import se.kth.livetech.properties.PropertyHierarchy;
import se.kth.livetech.properties.ui.TestTriangle;
import se.kth.livetech.util.DebugTrace;
import se.kth.livetech.util.Frame;
import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

public class LiveClient {
	public static int DEFAULT_PORT = 9099;
	// Any class constructable from String may be used
	// shortName, longName, pattern="regexp", defaultValue="text", description="text"
	// @see http://jewelcli.sourceforge.net/apidocs/uk/co/flamingpenguin/jewel/cli/Option.html
	public interface Options {
		@Option(shortName="s", longName="spider")
		boolean isSpider();
		
		@Option(longName="name")
		String getName();
		boolean isName();
		
		@Option(longName="autoname")
		boolean isAutoName();
		
		@Option(shortName="h",
				longName="address")
		String getLocalHost();
		boolean isLocalHost();

		@Option(shortName="p",
				longName="port")
		int getPort();
		boolean isPort();
		
		@Option(longName="ip")
		boolean isIp();

		@Option(shortName="k",
				longName="kattis")
		boolean isKattis();
		
		@Option(longName="kattis-host")
		String getKattisHost();
		boolean isKattisHost();
		
		@Option(longName="kattis-port")
		int getKattisPort();
		boolean isKattisPort();
		
		@Option(longName="kattis-uri")
		String getKattisUri();
		boolean isKattisUri();
		
		@Option(longName="file")
		String getFileName();
		boolean isFileName();

		@Option(longName="test-triangle")
		boolean isTestTriangle();
		
		@Option(longName="test-scoreboard")
		boolean isTestScoreboard();
		
		@Option(longName="test-team")
		boolean isTestTeam();

		@Option(longName="test-judge-queue")
		boolean isTestJudgeQueue();
		
		@Option(longName="live")
		boolean isLive();
		
		@Option(longName="fake", description="Fake contest.")
		boolean isFake();
		
		@Option(longName="control")
		boolean isControl();
		
		@Option(longName="fullscreen")
		boolean isFullscreen();
		
		@Option(longName="screen")
		int getScreen();
		boolean isScreen();
		
		@Option(helpRequest=true)
		boolean getHelp();
		
		@Option(longName="vnc")
		boolean isVnc();

		@Option(longName="vlc")
		boolean isVlc();

		@Unparsed
		List<String> getArgs();
		boolean isArgs();
	}
	public static class HostPort {
		String host;
   		int port;
		public HostPort(String addr) {
			String[] parts = addr.split(":");
			host = parts[0];
			port = Integer.parseInt(parts[1]);
		}
	}
	public static void main(String[] args) {
		try {
			// Parse options
			Options opts;
			try {
				opts = CliFactory.parseArguments(Options.class, args);
			} catch (ArgumentValidationException e) {
				System.err.println(e.getMessage());
				System.exit(1);
				return;
			}
			
			boolean spiderFlag = opts.isSpider() || !opts.isArgs();
			
			Frame fullscreenFrame = null;

			// Setup local node id
			String name = opts.isName() ? opts.getName() : opts.isAutoName() ? null : "noname";
			int port = DEFAULT_PORT;
			if (opts.isPort()) {
				port = opts.getPort();
			}
			NodeId localNode = Connector.getLocalNode(name, port);
			if (opts.isLocalHost()) {
				localNode.address = opts.getLocalHost();
			}
			if (opts.isIp()) {
				localNode.address = localNode.ip;
			}
			System.out.println("I am " + localNode);

			// Local state
			RedisLiveState localState = new RedisLiveState(spiderFlag);

			// Remote node registry
			NodeRegistry nodeRegistry = new NodeRegistry(localNode, localState);
			
			List<ContestUpdateListener> contestListeners = new ArrayList<ContestUpdateListener>();
			
			if (opts.isTestScoreboard()) {
				final ContestImpl c = new ContestImpl();
				IProperty prop_base = localState.getHierarchy().getProperty("live.clients." + localNode.name);
				final ScoreboardPresentation sp = new ScoreboardPresentation(c, prop_base);
				contestListeners.add(sp);
				Frame f = new Frame("TestContest", sp, null, false);
				if (opts.isFullscreen()) {
					fullscreenFrame = f;
				}
				else {
					f.pack();
					f.setVisible(true);
				}
			}
			if (opts.isTestTeam()) {
//				BROKEN:
//				final ContestImpl c = new ContestImpl();
//				final TeamPresentation tp = new TeamPresentation(c, base);
//				contestListeners.add(tp);
//				tp.setTeamId(1); // FIXME remove
//				Frame f = new Frame("TeamPresentation", tp, null, false);
//				if (opts.isFullscreen()) {
//					fullscreenFrame = f;
//				}
//				else {
//					f.pack();
//					f.setVisible(true);
//				}
			}
			if (opts.isTestJudgeQueue()) {
				final JudgeQueueTest jqt = new JudgeQueueTest();
				contestListeners.add(jqt);
				Frame f = new Frame("TestJudgeQueue", jqt, null, false);
				if (opts.isFullscreen()) {
					fullscreenFrame = f;
				}
				else {
					f.pack();
					f.setVisible(true);
				}
			}
			if (opts.isLive()) {
				final ContestImpl c = new ContestImpl();
				IProperty prop_base = localState.getHierarchy().getProperty("live.clients." + localNode.name);
				final LivePresentation lpr = new LivePresentation(c, prop_base, nodeRegistry.getRemoteTime(), fullscreenFrame);
				contestListeners.add(lpr);
				Frame f = new Frame("Live", lpr, null, false);
				f.setPreferredSize(new Dimension(1024, 576));

				if (opts.isFullscreen()) {
					fullscreenFrame = f;
				}
				else {
					f.pack();
					f.setVisible(true);
				}
			}

			// Add contest update listeners above!
			if (!contestListeners.isEmpty()) {
				final ContestReplayer cr = new ContestReplayer();
				localState.getContest(new ContestId("contest", 0)).addAttrsUpdateListener(cr);

				// ContestReplayControl
				IProperty prop_base = localState.getHierarchy().getProperty("live.clients." + localNode.name);
				final ContestReplayControl crc = new ContestReplayControl(cr, prop_base);
				cr.addContestUpdateListener(crc);

				for(ContestUpdateListener contestListener : contestListeners) {
					DebugTrace.trace("Contest listener: %s", contestListener);
					cr.addContestUpdateListener(contestListener);
				}
			}
			// Add contest update providers below!
			
			if (opts.isKattis()) {
				final KattisClient kattisClient;
				
				if (opts.isKattisHost()) {
					if (opts.isKattisPort()) {
						if (opts.isKattisUri()) {
							kattisClient = new KattisClient(opts.getKattisHost(), opts.getKattisPort(), opts.getKattisUri());
						} else {
							kattisClient = new KattisClient(opts.getKattisHost(), opts.getKattisPort());
						}
					} else {
						if (opts.isKattisUri()) {
							kattisClient = new KattisClient(opts.getKattisHost(), opts.getKattisUri());
						} else {
							kattisClient = new KattisClient(opts.getKattisHost());
						}
					}
				} else {
					kattisClient = new KattisClient();
				}
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
				final LogListener log = new LogListener("kattislog_"+dateFormat.format(new Date())+".txt");
				kattisClient.addAttrsUpdateListener(log);
				
				// TODO: nodeRegistry.addContest(new ContestId("contest", 0), kattisClient);
				kattisClient.addAttrsUpdateListener(localState.getContest(new ContestId("contest", 0)));

				kattisClient.startPushReading();

				localState.setContestSourceFlag(true);
			}
			if (opts.isFileName()) {
				try {
					final LogSpeaker logSpeaker = new LogSpeaker(opts.getFileName());
					// TODO: nodeRegistry.addContest(new ContestId("contest", 0), kattisClient);
					logSpeaker.addAttrsUpdateListener(localState.getContest(new ContestId("contest", 0)));
					try {
						logSpeaker.parse();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.exit(1);
				}
				localState.setContestSourceFlag(true);
			}
			if (opts.isFake()) {
				TestContest tc = new TestContest(100, 12, 15000);
				FakeContest fc = new FakeContest(tc);
				//TODO: nodeRegistry.addContest(new ContestId("contest", 0), tc);
				tc.addAttrsUpdateListener(localState.getContest(new ContestId("contest", 0)));
				fc.start();
				
				localState.setContestSourceFlag(true);
			}
			
			if (opts.isControl()) {
				PropertyHierarchy hierarchy = localState.getHierarchy();
				IProperty base = hierarchy.getProperty("live.control." + localNode.name);
				IProperty clients = hierarchy.getProperty("live.clients");
				new ProductionFrame(hierarchy, base, clients);
			}
			
			if (opts.isTestTriangle()) {
				TestTriangle.test(localState.getHierarchy());
			}
			
			if (opts.isVnc()) {
				PropertyHierarchy hierarchy = localState.getHierarchy();
				/*Frame foo = */new Frame("foo", new VNCPresentation(hierarchy.getProperty("live.clients.localhost.vnc")));
			}

			if (opts.isVlc()) {
				PropertyHierarchy hierarchy = localState.getHierarchy();
				VLCView view = new VLCView(hierarchy.getProperty("live.clients.localhost.vlc"), fullscreenFrame);
				/*Frame foo = */new Frame("bar", view);
			}
		
			// Listen!
			System.out.println("Listening on port " + port);
			LiveService.Iface handler = new BaseHandler(nodeRegistry);
			Connector.listen(handler, port, true);

			// Connect!
			if (opts.isArgs()) {
				for (String arg : opts.getArgs()) {
					System.out.println(arg);
					HostPort hostPort = new HostPort(arg);
					nodeRegistry.connect(hostPort.host, hostPort.port);
				}
			}
			
			if (fullscreenFrame != null) {
				int screen = opts.isScreen() ? opts.getScreen() : 0;
				fullscreenFrame.fullScreen(screen);
			}
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}