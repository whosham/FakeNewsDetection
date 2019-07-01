<?php
require "conn.php";


$location = $_POST["location"] ?? "";
$action = $_POST["action"] ?? "" ;

error_reporting( error_reporting() & ~E_NOTICE );

if ($action === "query_cc") {
    $mysql_qry = "select * from data where location  like '$location' ; " ;
    $result = mysqli_query($conn, $mysql_qry );
            $rows = array();
            while($r = mysqli_fetch_assoc($result)) {
                    $rows[] = $r;
             }
             echo json_encode($rows);
    }
$conn->close();

?>
