<?
if (isset($_POST['profile'])) {
   $profiles = json_decode(utf8_encode($_POST['profile']), true);
   $profile = $profiles[0];
   if (isset($profile) 
      && isset($profile['id']) 
      && isset($profile['username']) 
      && (isset($profile['password']) || isset($profile['socialnetworks'])) 
      && isset($profile['avatar']) 
      && isset($profile['gender']) 
      && isset($profile['birthdate']) 
      && isset($profile['email'])) {
         foreach($profile as $k => $v) $$k = $v;
         // Mininet Uesr Authentication
         if (isset($profile['password'])) {
            // TODO
         }
         // SN Authentication
         else {
            $snuserid = null;
            $snname = null;
            foreach ($socialnetworks as $sn) {
               $snuserid = $sn['id'];
               $snname = $sn['name'];
               break;
            }
            if (isset($snuserid) && isset($snname)) {
               require_once __DIR__ . '/db/db_connect.php';
               $db = new DB_CONNECT();

               $updated_fields = array('username', 'gender', 'birthdate');
               $updated_fields_str = array();
               foreach($updated_fields as $field) {
                  $updated_fields_str[] = "$field = '" . $$field . "'" ;
               } 
               $updated_fields_str = implode(",", $updated_fields_str);

               $query = "UPDATE Person SET " . $updated_fields_str
                  . ", avatar = COMPRESS('$avatar'), email = COMPRESS('$email')
                  WHERE id = '$id'
                  AND EXISTS (SELECT * FROM PersonSocialNetworkWithName
                  WHERE PersonSocialNetworkWithName.id = '$id'
                  AND PersonSocialNetworkWithName.socialnetwork = '$snname'
                  AND PersonSocialNetworkWithName.snuserid = '$snuserid');";

               $result = mysql_query($query); 

               $result &= mysql_query("DELETE FROM PersonInterest WHERE pid = '$id'");
               if (isset($interests)) {

                  require_once __DIR__ . '/db/db_util.php';
                  $result &= populateInterests( $id, $interests );
               }

               if ($result) {
                  return;
               }
            }
         }
      }
}

if (isset($_POST['privacy'])) {
   $privacies = json_decode($_POST['privacy'], true);
   $privacy = $privacies[0];
   if (isset($privacy)
      && isset($privacy['id'])
      && isset($privacy['level'])
      && isset($privacy['basic'])
      && isset($privacy['detailed'])
      && isset($privacy['location'])
   ) {
      require_once __DIR__ . '/db/db_connect.php';
      $db = new DB_CONNECT();

      foreach($privacy as $k => $v) $$k = $v;
      $fields = array('level', 'basic', 'location', 'detailed');
      $insert_fields_str = array();
      $updated_fields_str = array();
      foreach($fields as $field) {
         $insert_fields_str[] = "'". $$field. "'";
         $updated_fields_str[] = "$field = VALUES($field)" ;
      } 
      $insert_fields_str = implode(",", $insert_fields_str);
      $updated_fields_str = implode(",", $updated_fields_str);
      $query = "INSERT INTO Privacy VALUES('$id', $insert_fields_str)
         ON DUPLICATE KEY UPDATE $updated_fields_str;";
      $result = mysql_query($query); 
      if ($result) {
         return;
      }
   }
}

$response = array();
$response['error'] = "Update failed";
echo json_encode($response);
?>
