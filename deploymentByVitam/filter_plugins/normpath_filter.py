import os
from ansible import errors

def normpath(*paths):
    try:
        return  os.path.normpath( "/" .join(paths))
    except Exception as e:
        raise errors.AnsibleFilterError(
            'error joining path()'.str(e.message))

class FilterModule(object):
    ''' A filter to stringiify certificate content '''
    def filters(self):
        return {
            'normpath': normpath
        }
