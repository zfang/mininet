<?

if (isset($_GET['id']) && isset($_GET['blocked'])) {
   foreach($_GET as $k => $v) $$k = $v;

   require_once __DIR__ . '/db/db_connect.php';
   require_once __DIR__ . '/db/db_util.php';
   $db = new DB_CONNECT();

   $response = array();
   $result;

   if ($blocked === 'true') {
      $result = mysql_query("SELECT Id.id, Id.username, Basic.avatar, Basic.gender, Basic.birthdate, Basic.email FROM
         (SELECT * FROM PersonId
         WHERE EXISTS (SELECT 1 FROM Blocked WHERE Blocked.blockerid = '$id' AND Blocked.blockeeid = PersonId.id)) AS Id 
         LEFT JOIN 
         (SELECT * FROM PersonBasic
         WHERE EXISTS (SELECT 1 FROM Privacy WHERE PersonBasic.id = Privacy.pid AND Privacy.level IN ('Everyone') AND Privacy.basic = TRUE)) AS Basic 
         ON Id.id = Basic.id;") or die(mysql_error());
      if (mysql_num_rows($result) > 0) {
         while ($row = mysql_fetch_assoc($result)) {
            $response[] = $row;
         }
      }
   }
   else {
      $result = mysql_query("SELECT Id.id, Id.username, Basic.avatar, Basic.gender, Basic.birthdate, Basic.email FROM
         (SELECT * FROM PersonId
         WHERE EXISTS (SELECT 1 FROM Friend WHERE Friend.pid1 = '$id' AND Friend.pid2 = PersonId.id)
         AND NOT EXISTS (SELECT 1 FROM Blocked
         WHERE (Blocked.blockerid = '$id' AND Blocked.blockeeid = PersonId.id))) AS Id 
         LEFT JOIN
         (SELECT * FROM PersonBasic
         WHERE EXISTS (SELECT 1 FROM Privacy WHERE PersonBasic.id = Privacy.pid AND Privacy.level IN ('Everyone') AND Privacy.basic = TRUE)) AS Basic 
         ON Id.id = Basic.id;");
      if (mysql_num_rows($result) > 0) {
         while ($row = mysql_fetch_assoc($result)) {
            $matched_id = $row['id'];
            if (mysql_num_rows(mysql_query("SELECT * FROM Privacy 
               WHERE Privacy.pid = '$matched_id' 
               AND Privacy.level IN ('Everyone')
               AND Privacy.detailed = TRUE;")) > 0) {
                  $row = queryInterests($row); 
               }
            $response[] = $row;
         }
      }
   }

   echo json_encode($response);

   return;
}


if (!isset($_POST['id'])) {
   return;
}
foreach($_POST as $k => $v) $$k = $v;

if (isset($_POST['add'])) {
   require_once __DIR__ . '/db/db_connect.php';
   $db = new DB_CONNECT();
   $query = "SELECT 1 FROM Blocked 
      WHERE (Blocked.blockerid = '$id' AND Blocked.blockeeid = '$add')
      OR (Blocked.blockerid = '$add' AND Blocked.blockeeid = '$id');";
   $result = mysql_query($query);
   if (mysql_num_rows($result) === 0) {
      $query = "INSERT INTO Friend VALUES('$id', '$add');";
      mysql_query($query);
   }
}

if (isset($_POST['delete'])) {
   require_once __DIR__ . '/db/db_connect.php';
   $db = new DB_CONNECT();
   $query = "DELETE FROM Friend WHERE pid1 = '$id' AND pid2 = '$delete';";
   mysql_query($query);
   $query = "DELETE FROM Blocked WHERE blockerid = '$id' AND blockeeid = '$delete';";
   mysql_query($query);
}

if (isset($_POST['block'])) {
   require_once __DIR__ . '/db/db_connect.php';
   $db = new DB_CONNECT();
   $query = "INSERT INTO Blocked VALUES('$id', '$block');";
   mysql_query($query);
}

if (isset($_POST['unblock'])) {
   require_once __DIR__ . '/db/db_connect.php';
   $db = new DB_CONNECT();
   mysql_query($query);
   $query = "DELETE FROM Blocked WHERE blockerid = '$id' AND blockeeid = '$unblock';";
   mysql_query($query);
}

?>
