<?php
require "conn.php";


$user_name = $_POST["user_name"] ?? "";
$user_pass = $_POST["password"] ?? "" ;


error_reporting( error_reporting() & ~E_NOTICE );
$mysql_qry = "select * from login_data where (username like '$user_name' or email like '$user_name') and password like '$user_pass'  ; " ;
$result = mysqli_query($conn, $mysql_qry );
        $rows = array();
        while($r = mysqli_fetch_assoc($result)) {
                $rows[] = $r;
         }
         echo json_encode($rows);

$conn->close();

?>