import os
from ansible import errors
import re
cert_header_pattern = re.compile("^-----(\w*[ ]?)*-----$")


def cert_to_str(certificate_content):
    """
    Remove -----BEGIN ... ---- / -----END ...---- / \n\t\r characters from certificate content
    :param certificate_content:
    :return:
    """
    try:
         with open("/tmp/log","w") as f:
             f.write("".join([line for line in certificate_content.splitlines() if not cert_header_pattern.match(line)]))
         return "".join([line for line in certificate_content.splitlines() if not cert_header_pattern.match(line)])
    except Exception as e:
        raise errors.AnsibleFilterError(
            'certificate cannot be reduced to string ()'.str(e.message))


def normpath(*paths):
    return os.path.normpath(os.path.join(*paths))


class FilterModule(object):
    ''' A filter to stringiify certificate content '''
    def filters(self):
        return {
            'cert_to_str': cert_to_str,
            'normpath': normpath
        }

