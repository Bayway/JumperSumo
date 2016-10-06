<?php
$servername = "localhost";
$username = "baywayla_jumper";
$password = "jumpersumo!!7";
$dbname = "baywayla_jumper";

$ID = $_GET['id'];

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
} 

$sql = "UPDATE commands SET stato=1 WHERE id=" . $ID;

if ($conn->query($sql) === TRUE) {
    echo "1";
} else {
    echo "Error: " . $sql . "<br>" . $conn->error;
}

$conn->close();
?>