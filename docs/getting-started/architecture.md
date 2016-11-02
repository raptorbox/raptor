Architecture
===

The following picture depicts an high level view of Raptor architecture.

![Architecture](img/Raptor-Arch.png)

As a rapid prototyping platform, Raptor offers you a graphical web development environment and user interface simplifying the process of configuring and programming your devices in order to allow them to communicate with the cloud and (when needed) make them accessible remotely.

Virtual Devices
---

Logically each device you want to include in your applications is represented inside Raptor as a “virtual device”, i.e. the virtual representation of your device available for identification and interaction inside your applications.

Data storage
---

When your device produce new data (such as, for example, samples of the temperature in your room) it will use Raptor restful APIs in order to push this data into the cloud.

Data acquired by Raptor cloud will be automatically associated to the virtual representation of your device, stored into a scalable data store and made available for future usage and retrieval, business logic associated to your device in the cloud (i.e. subscriptions of applications interested in this data, workflows associated to your data flows) is automatically triggered and executed.

- Logically each device you want to include in your applications is represented inside Raptor as a “virtual device”, i.e. the virtual representation of your device available for identification and interaction inside your applications.

- When your device produce new data (such as, for example, samples of the temperature in your room) it will use Raptor restful APIs in order to push this data into the cloud. Data acquired by Raptor cloud will be automatically associated to the virtual representation of your device, stored into a scalable data store and so made available for future usage and retrieval, business logic associated to your device in the cloud (i.e. subscriptions of applications interested in this data, workflows associated to your data flows) is automatically triggered and executed.

*Note*: Definition and creation of your devices (of their virtual representation) can be done using Raptor graphical editor or programmatically via Raptor APIs (typically, the user interface is used for rapid prototyping and testing your solutions, while APIs are used to handle device provisioning according to your specific deployment needs in a programmatic way).  

The real-time Dashboard
---

Raptor offers indeed a monitoring environment (the Raptor dashboard) where you can check the status of your connected devices and verify acquired data through historical data visualizations and queries.

Once your devices are connected with the cloud and their data flows managed by Raptor cloud, it’s time to focus on creation of your application starting from the definition of its business logic. As for device creation, Raptor offer two possible ways that you can use in order to implement your application logic:

- use Raptor restful APIs to interact with devices and their data and build your application logic externally (using your preferred programming language and execution environment, such as, for example a java or PHP application, etc.)

- use Raptor Workflow Visual Editor to quickly define, test and verify your application logic combining data flows with external services, data sources, events

Workflow Visual Editor
---

Raptor Workflow Visual Editor is mainly aimed to support fast prototyping due to the nature of its capabilities: starting form data flows produced by your devices, the editor allow the creation of a workflow in a graphical way, according to a drag & drop paradigm, reusing existing processing capabilities configured inside existing nodes (selected from a palette of predefined nodes) offering specific functionalities to transform your data flows, combine them this other external and internal data sources, executing actions (such as, for example,  actuations on your devices or on external services, such as, for example, twitting a message).

As a rapid prototyping tool, the visual editor allows users to easily customize application logic predefined inside nodes made available by the editor (using javascript language) in order to quickly adapt the logic to your specific needs and objectives, test it and verify on the fly its behaviour).

Raptor Workflow Visual Editor is based on a well known open source solution [Node Red](http://nodered.org) that has been customized and adapted to this specific context.
