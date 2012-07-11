VFSServlet
==========

A simple servlet that allows you to return a file within a file using Apache Commons VFS

example call:

http://localhost:8080/files/path/to/somefile.zip!/path/to/somefile.jar!/path/to/index.txt

Will return the file /path/to/index.txt within somefile.jar
where somefile.jar is the file /path/to/somefile.jar within somefile.zip
where somefile.zip is the file /path/to/somefile.zip relative to the filesystem directory defined in the servlet initParam FileStorePath