//работает ниже до стриминга

//document.addEventListener("DOMContentLoaded", function() {
//    const sendButton = document.getElementById("send-button");
//    const chatInput = document.getElementById("chat-input");
//    const messagesContainer = document.getElementById("messages");
//
//    // Функция отправки сообщения (вынесена, чтобы не дублировать)
//    function sendMessage() {
//        const prompt = chatInput.value;
//        if (!prompt) return;
//        chatInput.value = "";
//
//        const userDiv = document.createElement("div");
//        userDiv.className = "message user";
//        userDiv.innerHTML = `<img src="/images/user.png" alt="User"><div class="bubble">${prompt}</div>`;
//        messagesContainer.appendChild(userDiv);
//
//        const pathParts = window.location.pathname.split("/");
//        const chatId = pathParts[pathParts.length - 1];
//
//        fetch(`/chat/${chatId}/entry`, {
//            method: 'POST',
//            headers: {
//                'Content-Type': 'application/x-www-form-urlencoded',
//            },
//            body: `prompt=${encodeURIComponent(prompt)}`
//        })
//        .then(response => {
//            if (response.redirected) {
//                window.location.href = response.url;
//            }
//        })
//        .catch(error => console.error('Error:', error));
//    }
//
//    // Отправка по клику на кнопку
//    sendButton.addEventListener("click", sendMessage);
//
//    // ✅ Отправка по нажатию Enter в поле ввода
//    chatInput.addEventListener("keydown", function(event) {
//        if (event.key === "Enter" && !event.shiftKey) {
//            event.preventDefault();  // предотвращает переход на новую строку
//            sendMessage();
//        }
//    });
//});

//а тут работает со стримингом
document.addEventListener("DOMContentLoaded", function() {
    const sendButton = document.getElementById("send-button");
    const chatInput = document.getElementById("chat-input");
    const messagesContainer = document.getElementById("messages");

    // Общая функция отправки
    function sendMessage() {
        const prompt = chatInput.value;
        if (!prompt) return;
        chatInput.value = "";

        // Добавляем сообщение пользователя в чат
        const userDiv = document.createElement("div");
        userDiv.className = "message user";
        userDiv.innerHTML = `<img src="/images/user.png" alt="User"><div class="bubble">${prompt}</div>`;
        messagesContainer.appendChild(userDiv);

        const pathParts = window.location.pathname.split("/");
        const chatId = pathParts[pathParts.length - 1];
        const url = `/chat-stream/${chatId}?userPrompt=${encodeURIComponent(prompt)}`;

        const eventSource = new EventSource(url);
        let fullText = "";

        // Создаем блок для ответа AI
        const aiDiv = document.createElement("div");
        aiDiv.className = "message mentor";
        aiDiv.innerHTML = `<img src="/images/mentor.png" alt="Mentor">`;
        const aiBubble = document.createElement("div");
        aiBubble.className = "bubble";
        aiDiv.appendChild(aiBubble);
        messagesContainer.appendChild(aiDiv);

        eventSource.onmessage = function(event) {
            const data = JSON.parse(event.data);
            let token = data.text;
            console.log(token);
            fullText += token;
            aiBubble.innerHTML = marked.parse(fullText);
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        };

        eventSource.onerror = function(e) {
            console.error("Ошибка SSE:", e);
            eventSource.close();
        };
    }

    // Отправка по клику на кнопку
    sendButton.addEventListener("click", sendMessage);

    // ✅ Отправка по нажатию Enter (без Shift)
    chatInput.addEventListener("keydown", function(event) {
        if (event.key === "Enter" && !event.shiftKey) {
            event.preventDefault();
            sendMessage();
        }
    });
});
