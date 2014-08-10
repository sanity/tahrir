var App = angular.module('App', []);
      App.controller('MessageCtrl', function ($scope){
        $scope.messages = ['hey, This is Tahrir'];

        $scope.addMessage = function() {
          $scope.messages.push($scope.enteredMessage);
          $scope.enteredMessage = '';
        };
      });