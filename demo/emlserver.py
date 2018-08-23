#
# Improved argument handling from the original EmlServer (https://djangosnippets.org/snippets/96/)
#
from datetime import datetime
import sys
import asyncore
from smtpd import SMTPServer

class EmlServer(SMTPServer):
    no = 0
    def process_message(self, peer, mailfrom, rcpttos, data):
        filename = 'message_%s-%d.eml' % (datetime.now().strftime('%Y-%m-%d_%H%M%S'),
                self.no)
        f = open(filename, 'w')
        f.write(data)
        f.close
        print 'New message saved: %s' % filename
        self.no += 1


def run(host, port):
    print ''
    print 'Starting EML Server at %s:%d' % (host, port)
    print ''
    foo = EmlServer((host, port), None)
    try:
        asyncore.loop()
    except KeyboardInterrupt:
        print ''
        print 'Stopping EML Server on keyboard interruption.'
        print ''


if __name__ == '__main__':
  port = 2525
  
  if len(sys.argv) > 1:
    port = int(sys.argv[1]);
  
  run('localhost', port)

