# MotionX — showcase plan

Prepared: 2026-10-07. This plan describes the implemented prototype; it does not add features or establish measurement accuracy. Current engineering status is in [README.md](../README.md).

## Positioning and problem statement

**MotionX turns a phone into a portable motion-observation and threshold-alert tool for small experiments and equipment checks.**

Use this problem statement on the opening slide:

> When a small setup starts moving or shaking, watching it alone does not give a repeatable motion reading or tell us exactly when it crosses a chosen limit. Students, makers, and workshop users need a quick way to observe that motion, measure what reaches a surface, and get immediate feedback using a phone they already have.

The concrete question is: **“Did this setup move beyond the limit I chose?”**

Lead with this observable task. Automatic fault diagnosis, failure prediction, industrial safety certification, and calibrated precision are not implemented or validated.

## Solution and value

For a visual explanation, open **Guide → How both tabs work** and expand the Visual, Physical, or shared alert flow. The [README flow diagrams](../README.md#how-both-tabs-work--flow-diagrams) provide the same paths for slides and developer handoff. They explain the pipeline; they do not display live execution status.

The current app provides three steps:

1. **See:** point a steady rear camera at a black marker. Visual shows displacement from its starting position in camera pixels and a live graph.
2. **Feel:** place the phone securely on the surface being investigated. Physical shows gravity-suppressed acceleration in g, RMS, peak, a live graph, and an estimated dominant frequency when the signal is suitable.
3. **React:** choose separate px and g limits. A crossing can produce a alert sound, vibration, and an on-screen alert. Both tabs share monitoring controls.

The camera measures relative image motion; the accelerometer measures the phone itself. These are complementary observations, not interchangeable units or automatic corroboration of the same object's motion. The phone must be mechanically coupled to a surface to measure that surface's transmitted acceleration.

No extra external sensor or internet connection is required for the demonstrated features. Signal processing runs locally. There is no implemented AI fault classifier, microphone analysis, gyro pipeline, session recording, or CSV export.

## Who it is for

| Audience | Task and value | Position today |
| --- | --- | --- |
| Engineering students and lab instructors | Demonstrate displacement, acceleration, frequency estimates, and threshold crossings in a tabletop experiment | Primary showcase audience |
| Makers and robotics teams | Explore wobble or shaking in a small prototype, and manually compare readings before and after an adjustment | Useful prototype workflow; hold placement and viewing geometry consistent |
| Workshop or equipment technicians | Explore changes in vibration transmitted to an accessible stationary surface during operation | Potential pilot use; needs repeatability and reference validation before diagnostic decisions |

Choose **one main story**: a student or maker checking a small vibrating setup. Mention technician use as a future application, not proof of industrial readiness.

## Main use case

**A maker wants to know when movement in a tabletop setup exceeds a chosen limit.**

First show visible movement with a marker. Then show physical movement transmitted through a tabletop. Finally show the app catching a threshold crossing without requiring the user to watch every graph point.

Office objects: a marker card, stable phone support, and a solid desk. Gentle taps on the desk demonstrate transmitted movement. Call this a **controlled bench demonstration**, not a reproduced equipment fault. A printer running on the same surface can be an optional real-equipment example only if its response has already been rehearsed; do not rely on it for the core demo.

## Real-life references for the script

Use these examples to explain why observing motion matters. The sources establish real applications; the suggested MotionX workflows are our proposed applications, not deployments, endorsements, or evidence of equivalent measurement performance. Sources checked: 2026-10-07.

| Real-life example and source | Presenter wording | Connection to MotionX |
| --- | --- | --- |
| **A washing machine shakes during a spin cycle.** Samsung identifies load balance and leveling as factors in excessive vibration. [Samsung support](https://www.samsung.com/ph/support/home-appliances/washing-machine-vibrating-and-noisy/) | “Think of a washing machine that starts shaking. You can see the movement, but how would you put a number to what you observe?” | Relatable opening example. A future controlled trial could compare transmitted acceleration at the same secured phone position under comparable conditions. MotionX cannot identify the cause or certify that an appliance is safe. |
| **Maintenance teams monitor motors, pumps, and fans.** Fluke describes vibration screening of rotating equipment and patterns associated with imbalance, misalignment, and looseness. [Fluke application guide](https://www.fluke.com/en-ph/learn/blog/condition-monitoring/when-and-how-to-use-and-set-up-vibration-monitoring-on-assets) | “In workshops, vibration measurements help teams investigate equipment condition. Our prototype brings basic motion observation to a phone for students and makers.” | Demonstrate movement transmitted through the desk, with the phone secured on a stationary surface. Present technician use as a future pilot; MotionX does not implement Fluke's diagnostic methods or professional sensor capabilities. |
| **Students study spring oscillations using a smartphone.** The phyphox Spring experiment uses phone acceleration to determine period and frequency. [phyphox experiment documentation](https://phyphox.org/wiki/index.php?title=Experiment:_Spring) | “In a physics lab, students already use phone sensors to study oscillation. MotionX lets us explore visible marker movement, physical acceleration, and chosen limits.” | Strongest current audience: students and instructors. Show Visual marker motion and Physical acceleration as separate demonstrations. A slow spring may fall below MotionX's nominal 2 Hz frequency minimum; do not promise a matching Hz result or imply the two apps use the same method. |

### Ready-to-say opening (about one minute)

> Think of a washing machine shaking during its spin cycle. Samsung's guidance includes checking load balance and leveling. In workshops, vibration monitoring is also used on motors, pumps, and fans, as Fluke describes. These are real situations where observing movement matters.
>
> Our starting point is a student or maker with a small moving setup: can they see how it moves and get an alert when it crosses a limit they choose?
>
> That is what MotionX demonstrates. With the phone held steady, Visual tracks a black marker's displacement in pixels. With the phone secured on the surface, Physical shows acceleration reaching the phone in g. Both have live graphs and adjustable thresholds, with a alert sound and vibration.
>
> Today we will demonstrate those steps using a marker and a desk. This prototype observes motion and threshold crossings; diagnosing equipment faults is future work that needs validation.

The opening's appliance and equipment claims are supported by the Samsung and Fluke links above. Put those links in slide notes; there is no need to read URLs aloud. Use the phyphox example when explaining the student audience or answering “Who would use this?”

## Setup and preparation

- Use the existing S24 FE build, one black dot/square on white paper, steady lighting, a stable phone support, and a solid desk. The printable marker is [marker.svg](marker.svg).
- Arrange the phone so the audience can see the screen. Use an existing screen-mirroring setup only if it already works; otherwise show the phone directly.
- Confirm camera permission and CAMERA READY. The current shared Start button requires a ready camera even when demonstrating Physical.
- Configure alert settings after the final app launch: they default to off and do not survive process death. A restart may require re-enabling them.
- For sound, select phone Sound mode, make Notifications volume audible, and enable Play alert sound. Media volume alone does not control this alert sound. It uses the supplied MP3, capped at 2.5 seconds per crossing. Perform one audible rehearsal; delivery has not yet been independently confirmed in the engineering record.
- Choose thresholds from the rehearsal: first observe the resting range, then the intended movement. Put the threshold above resting fluctuations and below a repeatable deliberate movement. Default 10 px / 0.1 g values are starting points, not universal recommendations or safety limits.
- Enable only the alert channel being demonstrated. Keep feedback off while discussing clean measurements; the phone's own beep/vibration can influence both pipelines.
- Presenter explains the task; a second person operates the marker and tabs. Agree on the cue “cross the limit.” If presenting alone, prepare the marker and phone placement before starting.

## Four-minute live script

| Time | Presenter says | Operator does / audience sees |
| --- | --- | --- |
| 0:00–0:30 | “Think of a washing machine shaking during a spin cycle. Now imagine a student or maker asking the same basic question about their setup: how much is it moving, and has it crossed my chosen limit? MotionX gives us visual displacement, physical acceleration, and an alert on one phone.” | Show the phone and marker. Use the washing-machine story as motivation, then introduce the controlled tabletop demo. Put the Samsung reference in slide notes. |
| 0:30–1:15 | “Visual measures how far this marker moves from its starting point, in camera pixels.” | Keep phone fixed, acquire marker, start monitoring, move it slowly, and show the displacement and graph. Remove the marker briefly to show unavailable tracking rather than false zero; reacquire before proceeding. |
| 1:15–2:00 | “Physical measures acceleration reaching the phone. It answers a different question from the camera.” | Stop before repositioning. Place the phone securely on the desk with camera access retained; restart, open Physical, let the baseline settle, then gently tap the same surface. Show current g, RMS, peak, and graph. Explain that 1 g is about 9.81 m/s². |
| 2:00–2:40 | “I choose the limit. MotionX calls attention to a crossing.” | Enable the rehearsed Physical threshold, show the dashed line, enable desired feedback, then repeat the gentle input. Audience sees the last-alert message and, if device settings allow, hears/feels feedback. Holding above the limit will not repeatedly alarm. |
| 2:40–3:10 | “The same control works for visible movement, using pixels.” | Disable Physical alerts. Stop and return the phone to its stable camera support; reacquire/start with a fresh marker reference. Enable the rehearsed Visual threshold and cross it once. This stage requires one prior marker-alert rehearsal; omit if not verified. |
| 3:10–3:35 | “The app also explains its measurements and estimates dominant frequency when a clear repeating signal exists.” | Open Guide. Briefly describe the Hz card. If it shows no clear frequency, explain that weak/irregular input is not given a confident number. Do not promise a Hz value from hand taps. |
| 3:35–4:00 | “MotionX makes small motion visible, measurable, and actionable using the phone already in your pocket.” | Close with the target user and next engineering step: reference validation, followed by saved sessions for comparisons. Distinguish implemented features from this roadmap. |

If you have only two minutes, demonstrate Visual, then one Physical threshold crossing. Omit the second alert and frequency discussion.

## Alert behavior to explain if asked

- Visual threshold: valid displacement in px. Physical threshold: current smoothed g matching the graph, not RMS or frequency.
- A shared 3-second cooldown follows an alert. Each affected channel needs a fresh reading below 90% of its threshold after cooldown to rearm. Earlier suppressed alerts are not replayed.
- Beeps and haptics can physically affect measurements. The cooldown helps avoid repeated feedback-triggered alerts; it does not remove contaminated values from the graphs, peak, or frequency window.
- Settings are user choices. The NORMAL/VIBRATING/HIGH status uses separate prototype RMS thresholds and is not a calibrated safety assessment.
- Alerts work while monitoring in the foreground, regardless of selected tab. Backgrounding stops monitoring and feedback.

## Five-slide deck

1. **Problem:** “Is it moving beyond my chosen limit?” Open with the washing-machine example, cite Samsung in the notes, then show the tabletop setup and intended student/maker user. The one-minute opening above can replace the short opening if the slot allows five minutes.
2. **Solution:** Camera → visual displacement; accelerometer → physical motion; threshold → immediate feedback.
3. **Live demo:** Run the script. Keep architecture out of the main product story.
4. **Who benefits:** Students/labs first, makers next, technician pilots after validation. Explain one task for each.
5. **Evidence and next step:** Real sensor streams and focused tests exist; basic movement/tab checks were user-reported passing. Reference accuracy, reproducibility, and frequency hardware comparison are still pending. Saved sessions/exports are proposed future work.

## Judge questions and exact answers

**“Does it predict a machine failure?”**
“Today it shows motion and alerts when a user-selected limit is crossed. Diagnosing a fault would require machine-specific data and validation.”

**“How accurate is it?”**
“We have tested processing with synthetic signals and exercised the live phone pipelines. We have not established calibrated displacement or acceleration accuracy, or verified frequency against a known source.”

**“Can it sense a machine across the room?”**
“The camera can track a visible marker relative to the camera. The accelerometer measures this phone, so physical vibration requires contact through an appropriate surface.”

**“Why both channels?”**
“They expose different motion: what appears to move in the camera image and what physically reaches the phone. We show both clearly rather than treating their units as equivalent.”

**“Is the displayed Hz motor RPM?”**
“It is an estimated strongest acceleration frequency, not necessarily the rotation rate or fundamental frequency. Harmonics and sampling limits matter.”

## Fallbacks and completion check

If tracking fails, improve marker contrast/light and reacquire once. If sound is muted, show the threshold message and check Notifications volume; do not pretend the beep played. If Hz is unavailable, explain the quality state rather than manufacturing periodic motion or showing sample data as live. A backup recording must be captured from the real app and labeled prerecorded; none was created for this plan.

One short rehearsal should confirm: visible marker movement, a Physical threshold crossing, audible feedback if promised, and a Visual alert only if included. Reuse the existing passing checks; no broad sensor retest is needed just for presenting. Stop monitoring at the end.

This document is a proposed script. Writing it does not mark those final rehearsal checks complete.
