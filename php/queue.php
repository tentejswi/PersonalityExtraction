<?php
        $link = mysql_connect('localhost', 'root', '') or die('Could not connect: ' . mysql_error());
        mysql_select_db('pe') or die('Could not select database');

        $username = $_GET["u"];
        if($username == null) {
                echo "invalid username";
                return;
        }

        $type = $_GET["s"];
        if($type == null) {
                echo "invalid type";
                return;
        }

        $query = "INSERT INTO user_queue(handle, type) values ('$username', '$type') ON DUPLICATE KEY UPDATE updated = NOW()";
        $result = mysql_query($query) or die('Query failed: ' . mysql_error());
        if($result == null) {
                echo "err";
                return;
        }

        echo "ok";
?>
