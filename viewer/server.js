var app = require('express').createServer();

app.get('/pe', function(req, res){
	var Client = require('mysql').Client;
	client = new Client();

	var u = req.param('u');

	client.user = 'root';
	client.password = '';
	client.host = 'localhost';
	client.port = 3306;
	client.connect();
	client.query('USE pe', function useDb(err, results, fields) {
		if (err) {
			console.log("ERROR: " + err.message);
			throw err;
		}
	});

	var buf = '';

	client.query('SELECT json FROM user_interests WHERE handle = \"' + u + '\"',function selectCb(err, results, fields) {
		if (err) {
			console.log("ERROR: " + err.message);
			res.send(err.message);
		} else if(results.length == 0) {
			res.send("no entry for user");
		} else {
			var buf = "<html><head><title>User Portrait</title><script type='text/javascript' \
    		    src='http://github.com/mbostock/protovis/raw/master/protovis.js'></script>\
		        </script>\
        		";
	        buf = buf + "<script> var user = " + results[0]['json'] + ";</script>";
    	    buf = buf + "</head><body><script type='text/javascript+protovis'>\
		        var vis = new pv.Panel().width(function() window.innerWidth - 1)\
			    .height(function() window.innerHeight - 1);\
		        var layout = vis.add(pv.Layout.Partition.Fill)\
			    .nodes(pv.dom(user).root('SomeName').nodes())\
			    .size(function(d) d.nodeValue)\
			    .order('ascending')\
			    .orient('radial');\
		        layout.node.add(pv.Wedge);\
		        layout.label.add(pv.Label)\
			    .visible(function(d) d.angle * d.innerRadius >= 7);\
		        vis.render();\
				</script>\
			    </body>\
		        </html>";
			res.send(buf);
		}
	});
	client.end();
});

app.listen(3000);

