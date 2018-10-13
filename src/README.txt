Direct to HTTP/src
'javac httpc'
'java httpc'
get -v http://httpbin.org/get?course=networking&assignment=1
get -v -h Content-Type:application/json http://httpbin.org/get?course=networking&assignment=1
post -h Content-Type:application/json -d '{"Assignment": 1}' http://httpbin.org/post
post -h Content-Type:application/json -d '{"assignment": 1, "courses": "network"}' http://httpbin.org/post
post -h Content-Type:application/json -f 'input.txt' http://httpbin.org/post
post -h Content-Type:application/json -f 'input.txt' http://httpbin.org/post -o output.txt

//redirection
get http://httpbin.org/redirect-to?url=http://httpbin.org/get
get -v http://httpbin.org/absolute-redirect/2