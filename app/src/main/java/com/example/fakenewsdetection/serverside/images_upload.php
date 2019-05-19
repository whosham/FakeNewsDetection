
<?php
require "conn.php";

//Don't forget to make directory writable by www-data

$image  = $_POST["image"] ?? "";

$image_url  = $_POST["image_url"] ?? "";

$action = $_POST["action"] ?? "";
$username = $_POST["username"] ?? "";
$upload_path="data/$image_url.jpg";


if ($action === "upload_image") {

             if( file_put_contents($upload_path,base64_decode($image)) !== false ) {
                 echo json_encode( array('response'=>'Image Uploaded Successfully') )  ;
             }
             else {
              echo json_encode(array('response'=>'Unable to write the file permission denied'));
             }

 }


$conn->close();
?>
