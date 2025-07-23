const nameInputNode = document.getElementById("prod-name-input");
const priceInputNode = document.getElementById("prod-price-input");
const imageInputNode = document.getElementById("prod-image-url-input");

const optionInputNode = document.getElementById("prod-option-input");
const optNameElement = optionInputNode.querySelector("input[name='opt-name']");
const optQuantityElement = optionInputNode.querySelector("input[name='opt-quantity']");
const optionListNode = document.getElementById("prod-option-list");


function addOption() {
    const addedOptionElement = document.createElement("tr");
    addedOptionElement.className = "py-1 h-10 text-center text-sm font-medium text-gray-600";
    addedOptionElement.innerHTML = `
        <td class="px-2 border text-left">${optNameElement.value}</td>
        <td class="px-2 border text-right">${optQuantityElement.value}</td>
        <td class="px-2 border">
            <button type="button" onClick="removeOption(this)"
                    class="text-red-600 hover:text-red-800 focus:outline-none text-center">
                삭제
            </button>
        </td>
    `;
    optionListNode.appendChild(addedOptionElement);
    optNameElement.value = "";
    optQuantityElement.value = "";
}

function removeOption(element) {
    const optionRow = element.parentNode.parentNode;
    optionListNode.removeChild(optionRow);
}

function extractOptions() {
    const options = [];
    const optionRows = optionListNode.querySelectorAll("tr");

    optionRows.forEach(row => {
        const cells = row.querySelectorAll("td");
        if (cells.length >= 2) {
            const name = cells[0].textContent.trim();
            const quantity = parseInt(cells[1].textContent.trim().replace(/,/g, ""));
            options.push({ name, quantity });
        }
    });
    return options;
}

async function requestCreateProduct() {
    const productData = {
        name: nameInputNode.value.trim(),
        price: parseInt(priceInputNode.value.trim().replace(/,/g, "")),
        imageUrl: imageInputNode.value.trim(),
        options: extractOptions()
    };
    try {
        const res = await fetch("/api/products", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(productData)
        });

        if (res.ok) {
            alert("상품이 성공적으로 생성되었습니다.");
            window.location.href = "/admin/products";
        } else {
            const errorData = await res.json();
            console.error("상품 생성 요청 실패:", errorData);
            if (errorData.validationErrors !== undefined && errorData.validationErrors.length > 0) {
                const errorMessages = errorData.validationErrors.map(err => `${err.field}: ${err.message}`).join("\n");
                alert(errorMessages);
            } else {
                alert(`상품 생성에 실패했습니다: ${errorData.detail}`);
            }
        }
    } catch (error) {
        console.error("상품 생성 중 오류 발생:", error);
        alert("상품 생성 중 오류가 발생했습니다. 다시 시도해주세요.");
    }
}