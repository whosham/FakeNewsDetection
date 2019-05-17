<?php
require "conn.php";


$email = $_POST["email"] ?? "";
$password = $_POST["password"] ?? "" ;
$action = $_POST["action"] ?? "" ;

error_reporting( error_reporting() & ~E_NOTICE );

if ($action === "check_login") {
    $mysql_qry = "select * from login where email like '$email' and password like '$password'  ; " ;
    $result = mysqli_query($conn, $mysql_qry );
            $rows = array();
            while($r = mysqli_fetch_assoc($result)) {
                    $rows[] = $r;
             }
             echo json_encode($rows);
    }
$conn->close();

?>