Running locally
===============

This project uses the Maven Cargo plugin to run Essentials, the CMS and site locally in Tomcat.

The demo includes Hippo document publication/depublication event handing to update/delete
documents in the embedded Solr Search Index by default.

Optionally, you can test the demo scenario with ElasticSearch engine instead of Solr.
Also, you can test it with ActiveMQ enabled to guarantee more reliable message deliveries.

To test the default scenario, execute the following in the project root folder:

    $ mvn clean verify
    $ mvn -P cargo.run

After your project is set up, you can access web applications:
    - CMS : http://localhost:8080/cms/           ( Content production webapp )
    - SITE : http://localhost:8080/site/         ( Content delivery webapp )
    - Solr Admin : http://localhost:8080/solr/   ( Solr Browse/Query/Update/Admin webapp)
    - Hawtio : http://localhost:8080/hawtio/     ( Hawtio webapp to monitor Camel, JVM, etc.)
    - Essentials : http://localhost:8080/essentials

Logs are located in target/tomcat7x/logs

Running Demo with ElasticSearch instead of Solr
===============================================

First, you need to download/install ElasticSearch to test with it locally.
Here are simplified steps for testing purpose only:

1. Download the latest version of ElasticSearch at http://www.elasticsearch.org/download/.
2. Extract the archive file into the project root folder.
   So, for example, you will have 'elasticsearch-x.x.x' subfolder in the project root folder.
3. (Optional) For your convenience, you can install an ElasticSearch frontend plugin like the following example:

    - Move to the ElasticSearch home directory:

        $ cd ./elasticsearch-*/

    - Run the following to install *elasticsearch-head* (https://github.com/mobz/elasticsearch-head),
      a web front end for an Elasticsearch cluster.

        $ sudo bin/plugin -install mobz/elasticsearch-head

4. Start ElasticSearch.

    - Open a new command line console and move to 'elasticsearch-x.x.x/bin' folder and run the following:

        $ cd ./elasticsearch-*/bin
        $ ./elasticsearch

    The above command will start a simple ElasticSearch as a foreground service at port 9200 and 9300 by default.

5. Check if your ElasticSearch is running:

    - If you installed *elasticsearch-head*, then just visit http://localhost:9200/_plugin/head/.

    - Otherwise, run commands like the following:

        $ curl -XGET 'http://localhost:9200/'

        (See more command examples in README.textile under the ElasticSearch home directory.)

6. Run Cargo with ElasticSearch integratino mode

    $ mvn clean verify
    $ mvn -Pcargo.run -Dcargo.jvm.args="-Dsearch.engine=es"


Running Demo with ActiveMQ instead of the default Camel File inbox
==================================================================

First, you need to download/install ActiveMQ to test with it locally.
Please follow the guide in http://activemq.apache.org/getting-started.html.

Here are simplified steps for testing purpose only:

    1. Download the latest ActiveMQ binary from http://activemq.apache.org/download.html.
    2. Extract the downloaded archive file to the project root folder.
       So, for example, you will have 'apache-activemq-x.x.x' subfolder in the project root folder.
    3. Open a new command line console and move to ./apache-activemq-x.x.x/bin/ folder.
    4. Run the following to start in console mode (You can type Ctrl+C to stop it):

        $ ./activemq console

    5. Checking/Monitoring

        Visit http://localhost:8161/admin/ (Login by admin/admin by default).

    6. Run Cargo with ActiveMQ mode

        $ mvn clean verify
        $ mvn -Pcargo.run -Dcargo.jvm.args="-Dqueue.mode=activemq"


E-mail Sending Component Test
=============================

E-mail sending is not so relevant with this forge module, but if you want to test with
Camel::Mail component as well for some reason, then you can add something like the following
in the route configuration:

    <to uri="smtp://localhost?javaMailSender=#javaMailSender&amp;..." />

See the example (commented out by default) in cms/src/main/webapp/WEB-INF/camel/routes-with-file.xml
and its dependent bean definitions ('jndiMailSession' bean and 'jndiMailSender' bean).

By default, the JNDI Mail Session resource is configured in conf/context.xml at port 2525.
If you don't have any SMTP server, you may run the following to emulate an SMTP server for testing purpose:

    $ python ./emlserver.py


Building distribution
=====================

To build a Tomcat distribution tarball containing only deployable artifacts:

  mvn clean verify
  mvn -P dist

See also src/main/assembly/distribution.xml if you need to customize the distribution.

Using JRebel
============

Set the environment variable REBEL_HOME to the directory containing jrebel.jar.

Build with:

  mvn clean verify -Djrebel

Start with:

  mvn -P cargo.run -Djrebel

Best Practice for development
=============================

Use the option -Drepo.path=/some/path/to/repository during start up. This will avoid
your repository to be cleared when you do a mvn clean.

For example start your project with:

  mvn -P cargo.run -Drepo.path=/home/usr/tmp/repo

or with jrebel:

  mvn -P cargo.run -Drepo.path=/home/usr/tmp/repo -Djrebel

Hot deploy
==========

To hot deploy, redeploy or undeploy the CMS or site:

  cd cms (or site)
  mvn cargo:redeploy (or cargo:undeploy, or cargo:deploy)

Automatic Export
================

Automatic export of repository changes to the filesystem is turned on by default. To control this behavior, log into
http://localhost:8080/cms/console and press the "Enable/Disable Auto Export" button at the top right. To set this
as the default for your project edit the file
./bootstrap/configuration/src/main/resources/configuration/modules/autoexport-module.xml

Monitoring with JMX Console
===========================
You may run the following command:

  jconsole

Now open the local process org.apache.catalina.startup.Bootstrap start
