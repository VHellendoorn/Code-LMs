/**
 *
 * Copyright (C) 2011 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.jclouds.examples.ec2.spot;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jclouds.aws.ec2.compute.AWSEC2TemplateOptions;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.predicates.NodePredicates;
import org.jclouds.net.IPSocket;
import org.jclouds.predicates.InetSocketAddressConnect;
import org.jclouds.predicates.RetryablePredicate;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

/**
 * This the Main class of an Application that demonstrates the use of the Amazon EC2 extensions by
 * creating a small spot server.
 * 
 * Usage is: java MainApp accesskeyid secretkey group command where command in create destroy
 * 
 * @author Adrian Cole
 */
public class MainApp {

   public static int PARAMETERS = 4;
   public static String INVALID_SYNTAX = "Invalid number of parameters. Syntax is: accesskeyid secretkey group command\nwhere command in create destroy";

   public static void main(String[] args) {

      if (args.length < PARAMETERS)
         throw new IllegalArgumentException(INVALID_SYNTAX);

      // Args
      String accesskeyid = args[0];
      String secretkey = args[1];
      String group = args[2];
      String command = args[3];

      // Init
      ComputeService compute = new ComputeServiceContextFactory().createContext("aws-ec2", accesskeyid, secretkey)
               .getComputeService();

      // wait up to 60 seconds for ssh to be accessible
      RetryablePredicate<IPSocket> socketTester = new RetryablePredicate<IPSocket>(new InetSocketAddressConnect(), 60,
               1, 1, TimeUnit.SECONDS);
      try {
         if (command.equals("create")) {

            Template template = compute.templateBuilder().build();

            template.getOptions().as(AWSEC2TemplateOptions.class)
            // set the price as 3 cents/hr
                     .spotPrice(0.03f)
                     // authorize my ssh key
                     .authorizePublicKey(
                              Files.toString(new File(System.getProperty("user.home") + "/.ssh/id_rsa.pub"),
                                       Charsets.UTF_8));

            System.out.printf(">> running one spot node type(%s) with ami(%s) in group(%s)%n", template.getHardware()
                     .getProviderId(), template.getImage().getId(), group);
            // run only a single node
            NodeMetadata node = Iterables.getOnlyElement(compute.createNodesInGroup(group, 1, template));

            System.out.printf("<< running node(%s)%n", node.getId());
            IPSocket socket = new IPSocket(Iterables.get(node.getPublicAddresses(), 0), node.getLoginPort());
            if (socketTester.apply(socket)) {
               System.out.printf("<< socket ready [%s] node(%s)%n", socket, node.getId());
               System.out.printf("ssh to node with the following command:%n ssh %s@%s%n",
                        node.getCredentials().identity, socket.getAddress());
               System.exit(0);
            } else {
               System.out.printf("<< socket not ready [%s] node(%s)%n", socket, node.getId());
            }
         } else if (command.equals("destroy")) {
            System.out.printf(">> destroying nodes in group(%s)%n", group);
            Set<? extends NodeMetadata> destroyed = compute.destroyNodesMatching(NodePredicates.inGroup(group));
            System.out.printf("<< destroyed(%d)%n", destroyed.size());
            System.exit(0);
         } else {
            System.err.println(INVALID_SYNTAX);
            System.exit(1);
         }
      } catch (RunNodesException e) {
         System.err.println(e.getMessage());
         for (NodeMetadata node : e.getNodeErrors().keySet())
            compute.destroyNode(node.getId());
         System.exit(1);
      } catch (IOException e) {
         System.err.println(e.getMessage());
         System.exit(1);
      } finally {
         compute.getContext().close();
      }

   }

}
