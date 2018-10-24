Direct to HTTP/src
'javac httpc'
'java httpc'
get -v http://httpbin.org/get?course=networking&assignment=1
get -v -h Content-Operation:application/json http://httpbin.org/get?course=networking&assignment=1
post -h Content-Operation:application/json -d '{"Assignment": 1}' http://httpbin.org/post
post -h Content-Operation:application/json -d '{"assignment": 1, "courses": "network"}' http://httpbin.org/post
post -h Content-Operation:application/json -f 'input.txt' http://httpbin.org/post
post -h Content-Operation:application/json -f 'input.txt' http://httpbin.org/post -o output.txt

//redirection
get http://httpbin.org/redirect-to?url=http://httpbin.org/get
get -v http://httpbin.org/absolute-redirect/2

Assignment2
javac -Djava.ext.dirs=./ httpfs.java
java -Djava.ext.dirs=./ httpfs
get -v http://localhost/
get  -v http://localhost/fresh.txt
post -d '{"assignment":1}' http://localhost/assignment.txt
get -v http://localhost/dfafsaf
get -v http://localhost/..