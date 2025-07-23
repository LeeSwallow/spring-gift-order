let isEdit = false;
let isOptionEdit = false;

const prodEditMode = {
    "prod-name": false,
    "prod-price": false,
    "prod-url": false,
};

const mapIdToKey = {
    "prod-name": "name",
    "prod-price": "price",
    "prod-url": "imageUrl",
};

const prodEditType = {
    "prod-name": "text",
    "prod-price": "number",
    "prod-url": "url",
};

const optionInputNode = document.getElementById("prod-option-input");

function enableEdit(id) {
    if (prodEditMode[id]) return;
    prodEditMode[id] = true;
    const editArea = document.getElementById(id);
    const textArea =  editArea.querySelector("p");
    const inputArea = editArea.querySelector("input");
    if (inputArea) {
        textArea.hidden = true;
        inputArea.type = prodEditType[id];
        inputArea.hidden = false;
    }
    if (!isEdit) showEditButton();
}

function showEditButton() {
    isEdit = true;
    const editButton = document.getElementById("btn-edit");
    editButton.hidden = false;
}

async function alertToResponse(action, response, onSuccess) {
    console.log(`${action} 요청 결과:`, response);
    if (response.ok) {
        alert(`${action}이(가) 성공적으로 완료되었습니다!`);
        if (onSuccess) onSuccess();
    } else {
        const errorData = await response.json();
        console.error(`${action} 요청 실패:`, errorData);
        if (errorData.validationErrors && errorData.validationErrors.length > 0) {
            const errorMessages = errorData.validationErrors.map(err => `${err.field}: ${err.message}`).join("\n");
            alert(errorMessages);
        } else if (errorData.detail) {
            alert(`${action}에 실패했습니다: ${errorData.detail}`);
        } else {
            alert(`${action} 중 알 수 없는 오류가 발생했습니다.`);
        }
    }
}

async function requestEditProduct() {
    const id = document.getElementById("prod-id").textContent;
    const requestBody = {}
    for (const key in prodEditMode) {
        if (prodEditMode[key]) {
            requestBody[mapIdToKey[key]] = document.getElementById(key).querySelector("input").value;
        }
    }
    console.log("수정 요청 본문:", requestBody);
    const res = await fetch(`/api/products/${id}`, {
        "method": "PUT",
        "headers": {
            "Content-Type": "application/json",
            "credentials": "include"
        }
        , "body": JSON.stringify(requestBody)
    });
    await alertToResponse("상품 수정", res, () => { window.location.reload(); });
}

function extractOptionFromRow(element) {
    const optionData = element.querySelectorAll("td");
    if (optionData.length < 3) return null;
    const id = parseInt(optionData[0].textContent.trim());
    const name = optionData[1].textContent.trim();
    const quantity = parseInt(optionData[2].textContent.trim());
    return { id, name, quantity };
}

function activateUpdate(element) {
    const optionRow = element.parentNode.parentNode;
    const optionData = extractOptionFromRow(optionRow);
    isOptionEdit = true;
    document.getElementById("add-option-btn").hidden = true;
    document.getElementById("update-option-btn").hidden = false;
    document.getElementById("change-option-title").textContent = `옵션 수정: ${optionData.name}`;
    optionInputNode.querySelector("input[name='opt-id']").value = optionData.id;
    optionInputNode.querySelector("input[name='opt-name']").value = optionData.name;
    optionInputNode.querySelector("input[name='opt-quantity']").value = optionData.quantity;
}

async function removeOption(element) {
    if (!confirm("정말로 이 옵션을 삭제하시겠습니까?")) return;

    const optionRow = element.parentNode.parentNode;
    const productId = parseInt(document.getElementById("prod-id").textContent);
    const optionData = extractOptionFromRow(optionRow);
    const res = await fetch(`/api/products/${productId}/options/${optionData.id}`, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
            "credentials": "include"
        }
    });
    await alertToResponse("옵션 삭제", res, () => { optionRow.remove(); });
}

async function addOption() {
    const productId = parseInt(document.getElementById("prod-id").textContent);
    const optionName = optionInputNode.querySelector("input[name='opt-name']").value.trim();
    const optionQuantity = parseInt(optionInputNode.querySelector("input[name='opt-quantity']").value.trim());

    const res = await fetch(`/api/products/${productId}/options`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "credentials": "include"
        },
        body: JSON.stringify({ name: optionName, quantity: optionQuantity })
    });
    await alertToResponse("옵션 추가", res, async () => {
        window.location.reload()}
    );
}

async function updateOption() {
    const productId = parseInt(document.getElementById("prod-id").textContent);
    const optionId = parseInt(optionInputNode.querySelector("input[name='opt-id']").value.trim());
    const optionName = optionInputNode.querySelector("input[name='opt-name']").value.trim();
    const optionQuantity = parseInt(optionInputNode.querySelector("input[name='opt-quantity']").value.trim());

    const res = await fetch(`/api/products/${productId}/options/${optionId}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "credentials": "include"
        },
        body: JSON.stringify({ name: optionName, quantity: optionQuantity })
    });
    await alertToResponse("옵션 수정", res, async () => {
        window.location.reload();
    });
}

