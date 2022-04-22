/**
 * Instantiate the app, the 'myApp' parameter must match
 * what is in ng-app
 */
var myApp = angular.module('myApp', ['ngRoute', 'ngCookies']);

myApp.config(['$routeProvider', '$httpProvider', '$locationProvider',
    function ($routeProvider, $httpProvider, $locationProvider) {
        $routeProvider.when('/authenticate', {
            templateUrl: 'logon.html'
        }).when('/content', {
            templateUrl: 'content.html'
        });
        $httpProvider.interceptors.push('AuthHeaderInterceptor');
        $httpProvider.interceptors.push('httpResponseInterceptor');

        $locationProvider.hashPrefix('');
    }
]);

myApp.factory('AuthHeaderInterceptor',
    function ($q, $cookies) {
        return {
            request: function (config) {
                console.log('inside interceptor' + $cookies.get('CSRF'));
                config.headers = config.headers || {};
                config.headers['XSRF'] = $cookies.get('CSRF');
                return config || $q.when(config);
            }
        };
    });

// http interceptor to handle redirection to login on 401, 403 response from API
myApp.factory('httpResponseInterceptor',
    ['$q', '$rootScope', '$location', function ($q, $rootScope, $location) {
        return {
            responseError: function (rejection) {
                if (rejection.status === 401 || rejection.status === 403) {
                    // Something like below:
                    $location.path('authenticate');
                } else if (rejection.status === 400) {
                    $location.path('content');
                }
                return $q.reject(rejection);
            }
        };
    }]);

myApp.controller('AccessCtrl',
    ['$scope', '$http', '$location', '$cookies', function ($scope, $http, $location, $cookies) {

        $scope.items = [];
        $scope.getItems = function () {
            $http({
                method: 'GET',
                url: 'http://localhost:8080/resources'
            }).then(function (response) {
                $scope.items = response.data;
            }, function (error) {
                console.log("Error loading resources" + error);
            });
        };

        $scope.click = function (resourceUrl) {
            $scope.resourceContent = '';
            $http({
                method: 'GET',
                url: resourceUrl
            }).then(function (response) {
                $scope.resourceContent = response.data;
                $location.path('content');
            }, function (error) {
                console.log("Error loading resources" + error);
                $scope.resourceContent = error.data.message
            });
        };

        $scope.user = {};
        $scope.login = function () {
            $scope.resourceContent = '';
            let credentials = $scope.user.username + ':' + $scope.user.password;
            $http({
                method: 'POST',
                url: 'http://localhost:8080/access',
                headers: {
                    'Authorization': 'Basic ' + btoa(credentials)
                },
            }).then(function (response) {
                $scope.resourceContent = response.data;
                $location.path('content');
            }, function (error) {
                $scope.resourceContent = 'Unable to log in';
            });
        };

    }]);
