package org.deeplearning4j.aws.ec2.provision;

import java.util.List;
import java.util.concurrent.Callable;

import akka.actor.ActorSystem;
import akka.dispatch.Futures;
import akka.dispatch.OnComplete;
import org.deeplearning4j.aws.ec2.Ec2BoxCreator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;

/**
 * Sets up a DL4J cluster
 * @author Adam Gibson
 *
 */
public class ClusterSetup {

	@Option(name = "-w",usage = "Number of workers")
	private int numWorkers = 1;
	@Option(name = "-ami",usage = "Amazon machine image: default, amazon linux (only works with RHEL right now")
	private String ami = "ami-fb8e9292";
	@Option(name = "-s",usage = "size of instance: default m1.medium")
	private String size = "m3.xlarge";
	@Option(name = "-sg",usage = "security group, this needs to be applyTransformToDestination")
	private String securityGroupName;
	@Option(name = "-kp",usage = "key pair name, also needs to be applyTransformToDestination.")
	private String keyPairName;
	@Option(name = "-kpath",usage = "path to private key - needs to be applyTransformToDestination, this is used to login to amazon.")
	private String pathToPrivateKey;
    @Option(name = "-wscript", usage = "path to worker script to run, this will allow customization of dependencies")
	private String workerSetupScriptPath;
	@Option(name = "-mscript", usage = "path to master script to run this will allow customization of the dependencies")
	private String masterSetupScriptPath;

    private ActorSystem as;

	private static Logger log = LoggerFactory.getLogger(ClusterSetup.class);


	public ClusterSetup(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			parser.printUsage(System.err);
			log.error("Unable to parse args",e);
		}


	}

	public void exec() {
		//master + workers
		Ec2BoxCreator boxCreator = new Ec2BoxCreator(ami,numWorkers,size,securityGroupName,keyPairName);
		boxCreator.create();
		boxCreator.blockTillAllRunning();
		List<String> hosts = boxCreator.getHosts();
		//provisionMaster(hosts.getFromOrigin(0));
		provisionWorkers(hosts);


	}






	private void provisionWorkers(List<String> workers) {
		as = ActorSystem.create("Workers");
        for(final String workerHost : workers) {
			try {
                Future<Void> f = Futures.future(new Callable<Void>(){

                    /**
                     * Computes a result, or throws an exception if unable to do so.
                     *
                     * @return computed result
                     * @throws Exception if unable to compute a result
                     */
                    @Override
                    public Void call() throws Exception {

                        HostProvisioner uploader = new HostProvisioner(workerHost, "ec2-user");
                        uploader.addKeyFile(pathToPrivateKey);
                        //uploader.runRemoteCommand("sudo hostname " + workerHost);
                        uploader.uploadAndRun(workerSetupScriptPath, "");
                        return null;
                    }
                },as.dispatcher());
                f.onComplete(new OnComplete<Void>() {
                    @Override
                    public void onComplete(Throwable throwable, Void aVoid) throws Throwable {
                        if(throwable != null)
                            throw throwable;
                    }
                },as.dispatcher());


			}catch(Exception e) {
				log.error("Error ",e);
			}
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ClusterSetup(args).exec();
	}

}
