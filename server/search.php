<?

if (!isset($_GET['id'])) {
   return;
}

$id = $_GET['id'];

$query = "";

if (isset($_GET['q'])) {

   $str = explode(",", $_GET['q']);
   $gender = null;
   $birthdate = null;
   $interests = null;
   foreach($str as $s) {
      $row = explode(":", $s);
      if ($row[0] === 'gender') {
         $gender = $row[1];
      } 
      else if ($row[0] === 'birthdate') {
         $birthdate = $row[1];
      } 
      else if ($row[0] === 'interests') {
         $interests_array = explode(".", $row[1]);
         $interests = array();
         $interests = $interests_array;
         foreach($interests_array as $interest) {
            $interest_last_word = end(explode(" ", $interest));
            if ($interest_last_word !== $interest) {
               $interests[] = $interest_last_word;
            }
         }
         $interests = implode("|", $interests);
      }
   }

   if (isset($gender) && $gender !== "") {
      $query .= " AND gender = '$gender'";
   }
   if (isset($birthdate) && $birthdate !== "") {
      $query .= " AND ABS(DATEDIFF(birthdate, '$birthdate')) < 365";
   }
   if (isset($interests) && $interests !== "") {
      $query .= " AND LOWER(interest) REGEXP LOWER('$interests')";
   }

}

$query = "SELECT DISTINCT id, username, avatar, gender, birthdate, email FROM 
   (SELECT IdBasic.*, Detailed.interest FROM
   (SELECT Id.id, Id.username, Basic.avatar, Basic.gender, Basic.birthdate, Basic.email FROM
   (SELECT * FROM PersonId) AS Id 
   LEFT JOIN 
   (SELECT * FROM PersonBasic
   WHERE EXISTS (SELECT * FROM Privacy WHERE PersonBasic.id = Privacy.pid AND Privacy.level IN ('Everyone') AND Privacy.basic = TRUE)) AS Basic
   ON Id.id = Basic.id) AS IdBasic
   LEFT JOIN
   (SELECT * FROM PersonDetailed 
   WHERE EXISTS (SELECT * FROM Privacy WHERE PersonDetailed.id = Privacy.pid AND Privacy.level IN ('Everyone') AND Privacy.detailed = TRUE)) AS Detailed
   ON IdBasic.id = Detailed.id) AS CompleteInfo 
   WHERE NOT EXISTS (SELECT * FROM Blocked
   WHERE (Blocked.blockerid = '$id' AND Blocked.blockeeid = CompleteInfo.id)
   OR (Blocked.blockeeid = '$id' AND Blocked.blockerid = CompleteInfo.id)) 
   AND id != '$id' $query;";

require_once __DIR__ . '/db/db_connect.php';
require_once __DIR__ . '/db/db_util.php';
$db = new DB_CONNECT();

$result = mysql_query($query);

$response = array();
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

echo json_encode($response);
?>
