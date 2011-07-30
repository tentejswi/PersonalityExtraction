<?php
	$link = mysql_connect('localhost', 'root', '') or die('Could not connect: ' . mysql_error());
	mysql_select_db('pe') or die('Could not select database');
	
	$username = $_GET["u"];
	if($username == null) {
		echo "invalid username";
		return;
	}
	
	$query = "SELECT json FROM user_interests WHERE handle like '$username'";
	$result = mysql_query($query) or die('Query failed: ' . mysql_error());
	if($result != null) {
		$arr = mysql_fetch_array($result, MYSQL_ASSOC);
		echo print_r($arr['json'], true);
	}
?>
