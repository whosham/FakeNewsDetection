<?php
require "conn.php";


$email = $_POST["email"] ?? "";
$password = $_POST["password"] ?? "" ;
$action = $_POST["action"] ?? "" ;

error_reporting( error_reporting() & ~E_NOTICE );

if ($action === "check_user") {
    $mysql_qry = "select email from login where email like '$email' ; " ;
    $result = mysqli_query($conn, $mysql_qry );
            $rows = array();
            while($r = mysqli_fetch_assoc($result)) {
                    $rows[] = $r;
             }
             echo json_encode($rows);
    }


if ($action === "register") {
    $mysql_qry = "insert into login ( email,password) values ('$email','$password')  ; " ;
    if ($conn->query($mysql_qry) === TRUE ) {
        echo "Success";
    }
    else {
        echo "Error: " . $mysql_qry . "<br>" . $conn->error ;
    }
}

$conn->close();

?>
