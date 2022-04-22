<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml">
<html ng-app="myApp">

<head lang="en">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
    <meta http-equiv="cache-control" content="no-cache"/>
    <meta http-equiv="expires" content="0"/>
    <meta http-equiv="pragma" content="no-cache"/>
    <meta content="text/html; charset=ISO-8859-1" http-equiv="Content-Type"/>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <script data-require="angular.js@1.6.6" data-semver="1.6.6"
            src="https://cdnjs.cloudflare.com/ajax/libs/angular.js/1.6.6/angular.js"></script>
    <script data-require="angular-resource@1.4.1" data-semver="1.4.1"
            src="https://code.angularjs.org/1.4.1/angular-resource.js"></script>
    <script data-semver="1.4.1"
            src="https://code.angularjs.org/1.4.1/angular-route.min.js"></script>
    <script data-semver="1.4.1"
            src="https://code.angularjs.org/1.4.1/angular-cookies.min.js"></script>
    <link href="css/bootstrap.css" rel="stylesheet"/>
    <link href="css/app.css" rel="stylesheet"/>
    <script src="js/app/script.js"></script>
</head>

<body>

<div ng-controller="AccessCtrl" ng-init="getItems()" class="generic-container">

    <img src="https://media.giphy.com/media/njYrp176NQsHS/giphy.gif" alt="You shall not pass!!!"
         class="panel panel-default"/>

    <div class="panel-heading"><span class="lead">All available resources</span></div>
    <div class="panel-body">
        <div class="formcontainer">
            <div ng-repeat="list in items.resources">
                <button ng-click="click(list)">{{list}}</button>
            </div>
        </div>
    </div>

    <script type="text/ng-template" id="logon.html">
        <div class="row">
            <div class="form-group col-md-12">
                <label class="alert alert-warning">Say "Friend" And Enter - Gandalf</label>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <p><strong>Login Page</strong></p>

                <form ng-submit="login()" class="form">
                    <div class="col-md-4">
                        <div class="form-group">
                            <label>
                                <input type="text" class="form-control" ng-model="user.username"
                                       placeholder="1#bob" required=""/>
                            </label>
                        </div>

                        <div class="form-group">
                            <label>
                                <input type="password" class="form-control" ng-model="user.password"
                                       placeholder="password" required=""/>
                            </label>
                        </div>

                        <div class="form-group">
                            <button type="submit" class="btn btn-success">Login</button>
                            <span class="text-danger" style="color:red">{{ error }}</span>
                        </div>

                    </div>
                </form>
            </div>
        </div>
    </script>

    <script type="text/ng-template" id="content.html">
        <div class="panel panel-default">
            <div class="panel-body">
                <p>{{resourceContent}}</p>
            </div>
        </div>

    </script>


    <div ng-view>
        Loading...
    </div>

</div>
</body>

</html>
