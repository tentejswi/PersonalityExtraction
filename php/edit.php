<?php
        $link = mysql_connect('localhost', 'root', '') or die('Could not connect: ' . mysql_error());
        mysql_select_db('pe') or die('Could not select database');

        $username = $_POST["u"];
        if($username == null) {
                echo "invalid username";
                return;
        }
        $json = $_POST["json"];
        if($json == null) {
                ecjo "invalid json";
                return;
        }

        $query = "UPDATE user_intersts SET json = \"$json\" WHERE handle = '$username'";
        $result = mysql_query($query) or die('Query failed: ' . mysql_error());
        if($result == null) {
                echo "err";
                return;
        }

        echo "ok";
?>
