<?php
$servername = "localhost";
$username = "baywayla_jumper";
$password = "jumpersumo!!7";
$dbname = "baywayla_jumper";

$ID = $_GET['id'];
$TIMESTAMP = $_GET['timestamp'];

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
} 

$sql = "SELECT id, text, stato, timestamp FROM commands WHERE id > " . $ID . " AND timestamp < " . $TIMESTAMP . " AND stato = 0";
$sql1 = "SELECT id, text, stato, timestamp FROM commands WHERE id > " . $ID . " AND stato = 0";

if ($TIMESTAMP != null)
	$result = $conn->query($sql);
else
	$result = $conn->query($sql1);

if ($result->num_rows > 0) {
    // output data of each row
    while($row = $result->fetch_assoc()) {
        echo "id: " . $row["id"]. " - msg: " . $row["text"]. " - " . $row["timestamp"]. "<br>";
    }
} else {
    echo "0 results";
}
$conn->close();
?>