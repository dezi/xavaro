WebAppRequest.onVoiceIntent = function(intent)
{
    var ca = calculator;

    ca.command = intent.command.toLowerCase();

    //
    // Remove action vom command.
    //

    ca.command = ca.command.replace("was ergibt", "").trim();
    ca.command = ca.command.replace("wie viel ist", "").trim();

    //
    // Reformat written numbers.
    //

    ca.command = " " + ca.command + " ";
    ca.command = ca.command.replace(" eins ", " 1 ");
    ca.command = ca.command.replace(" zwei ", " 2 ");
    ca.command = ca.command.replace(" drei ", " 3 ");
    ca.command = ca.command.replace(" vier ", " 4 ");
    ca.command = ca.command.replace(" fünf ", " 5 ");
    ca.command = ca.command.replace(" sechs ", " 6 ");
    ca.command = ca.command.replace(" sieben ", " 7 ");
    ca.command = ca.command.replace(" acht ", " 8 ");
    ca.command = ca.command.replace(" neun ", " 9 ");
    ca.command = ca.command.trim();

    //
    // Reformat special characters.
    //

    ca.command = ca.command.replace(",", ".").trim();
    ca.command = ca.command.replace("  ", " ").trim();

    console.log("WebAppRequest.onVoiceIntent: ===========> " + intent.command);

    ca.parts = ca.command.split(" ");

    ca.prev = null;
    ca.what = null;
    ca.next = null;

    for (var inx = 0; inx < ca.parts.length; inx++)
    {
        var part = ca.parts[ inx ];

        if (part == "+") { ca.what = "+"; continue; }
        if (part == "und") { ca.what = "+"; continue; }
        if (part == "plus") { ca.what = "+"; continue; }

        if (part == "minus") { ca.what = "-"; continue; }
        if (part == "weniger") { ca.what = "-"; continue; }

        if (part == "x") { ca.what = "*"; continue; }

        if (part == "durch") { ca.what = "/"; continue; }
        if (part == "geteilt") { ca.what = "/"; continue; }

        if (part == "wurzel") { ca.what = "sqrt"; continue; }
        if (part == "quadrat") { ca.what = "square"; continue; }

        if (part == "mehrwertsteuer")
        {
            if (ca.what == "+") ca.what = "*";
            if (ca.what == "-") ca.what = "/";

            ca.next = "1.19";

            calculator.compute();

            continue;
        }

        if (part.match(/[0-9\.]+/))
        {
            ca.next = parseFloat(part);

            calculator.compute();

            continue;
        }

        if (part.match(/[0-9]+/))
        {
            ca.next = parseInt(part);

            calculator.compute();

            continue;
        }
    }

    if (ca.prev == "Infinity") ca.prev = "eine Division durch null";

    if (ca.prev == "42") ca.prev = "42, die Antwort auf die Frage nach dem Leben, dem Universum und dem ganzen Rest";

    WebAppSpeak.speak(ca.command + " ergibt " + ca.prev);
}

calculator.compute = function()
{
    var ca = calculator;

    console.log("calculator.compute:" + ca.prev + " <=> " + ca.what + " <=> " + ca.next);

    if ((ca.prev != null) && (ca.what != null) && (ca.next != null))
    {
        if (ca.what == "+") ca.prev = ca.prev + ca.next;
        if (ca.what == "-") ca.prev = ca.prev - ca.next;
        if (ca.what == "*") ca.prev = ca.prev * ca.next;
        if (ca.what == "/") ca.prev = ca.prev / ca.next;

        ca.what = null;
        ca.next = null;

        return;
    }

    if ((ca.what != null) && (ca.next != null))
    {
        if (ca.what == "sqrt") ca.prev = Math.sqrt(ca.next);
        if (ca.what == "square") ca.prev = ca.next * ca.next;

        ca.what = null;
        ca.next = null;

        return;
    }

    if ((ca.what != null) && (ca.prev != null))
    {
        if (ca.what == "sqrt") ca.prev = Math.sqrt(ca.prev);
        if (ca.what == "square") ca.prev = ca.next * ca.prev;

        ca.what = null;
        ca.next = null;

        return;
    }

    ca.prev = ca.next;
    ca.next = null;
}
