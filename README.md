IoT Broker Application

Hosted at https://github.com/jonayerdi/leshan-IoT
This project is based on https://github.com/eclipse/leshan, most of the features and structure are inherited.

HOW TO RUN
-Run the project with leshan-server-demo/org.eclipse.leshan.server.demo/LeshanServerDemo.java as the main class
-For a LWM2M test client, leshan-client-demo/org.eclipse.leshan.client.demo/LeshanClientDemo.java can be used, it's both a sensor and a light
-The APIs are implemented on a server running in port 8080 (e.g. http://localhost:8080/api/lights) (The original Leshan servlets are still running)
-The User App is running in port 8090 (http://localhost:8090), the valid login credentials are loaded at startup from users.txt located in this directory
-For the lights to show up in the user app, their User ID must be the same as the UserID used to login (security can be bypassed by directly writing the endpoint name on the textbox below)
-When an endpoint appears on the list, click on it and its name should appear on the textbox below, then use the sliders to change the color

STRUCTURE CHANGES
-leshan-core
	-Added objectspecs.json to resources (light and sensor LWM2M object specs)
-leshan-client-demo
	-MyLight and MySensor classes added, implementing light and sensor LWM2M instances
-leshan-server-demo
	-userapp added to resources (static web page) (the setup of the server for this app is in LeshanServerDemo.java, right after the servlets and before starting the lwServer)
	-LightServlet, SensorServlet, ObserveServlet, UsersServlet and UserappServlet added to servlet package
	-ClientFormat, ClientFormatSerializer and UserDeserializer added to serializers package